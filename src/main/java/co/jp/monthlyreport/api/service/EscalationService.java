package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.dto.request.EscalationCreateRequest;
import co.jp.monthlyreport.api.dto.request.EscalationLogAddRequest;
import co.jp.monthlyreport.api.dto.request.EscalationSearchRequest;
import co.jp.monthlyreport.api.dto.request.EscalationUpdateRequest;
import co.jp.monthlyreport.api.model.EscalationLogRecord;
import co.jp.monthlyreport.api.model.EscalationRecord;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.InMemoryDataStore;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
/**
 * エスカレーションの検索・詳細・作成・更新・ログ追加を扱うサービス。
 * インプット: 認証ユーザー情報と各 API リクエスト。
 * アウトプット: エスカレーションデータまたは処理結果。
 */
public class EscalationService {

  private static final Set<String> VALID_SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH");
  private static final Set<String> VALID_STATUSES = Set.of("PENDING", "ONGOING", "RESOLVED");

  private final InMemoryDataStore dataStore;
  private final MessageSource messageSource;

  public EscalationService(InMemoryDataStore dataStore, MessageSource messageSource) {
    this.dataStore = dataStore;
    this.messageSource = messageSource;
  }

  /**
   * ロール別スコープでエスカレーション一覧を検索する。
   * インプット: user 認証ユーザー、request 検索条件。
   * アウトプット: エスカレーション一覧とページング情報。
   *
   * @param user    認証ユーザー
   * @param request 検索条件
   * @return 検索結果
   */
  public Map<String, Object> search(AuthUser user, EscalationSearchRequest request) {
    requireEscalationAccess(user);

    int page = request.page() == null ? 1 : request.page();
    int size = request.size() == null ? 20 : request.size();
    String status = request.status() == null || request.status().isBlank() ? "ALL" : request.status().toUpperCase(Locale.ROOT);

    List<EscalationRecord> filtered = scopedEscalations(user).stream()
        .filter(e -> "ALL".equals(status) || status.equals(e.getStatus()))
        .sorted(Comparator.comparing(EscalationRecord::getUpdatedAt).reversed())
        .collect(Collectors.toList());

    int from = Math.min((page - 1) * size, filtered.size());
    int to = Math.min(from + size, filtered.size());
    int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

    List<Map<String, Object>> items = filtered.subList(from, to).stream().map(e -> {
      Map<String, Object> item = new HashMap<>();
      item.put(ResponseKeys.ESCALATION_ID, e.getEscalationId());
      item.put(ResponseKeys.ESC_TITLE, e.getTitle());
      item.put(ResponseKeys.TARGET_EMPLOYEE_NAME, e.getTargetEmployeeName());
      item.put(ResponseKeys.TARGET_TEAM, e.getTargetTeam());
      item.put(ResponseKeys.SEVERITY, e.getSeverity());
      item.put(ResponseKeys.STATUS, e.getStatus());
      item.put(ResponseKeys.CREATED_BY_NAME, e.getCreatedByName());
      item.put(ResponseKeys.UPDATED_AT, e.getUpdatedAt().toString());
      return item;
    }).toList();

    return Map.of(
        ResponseKeys.ITEMS, items,
        ResponseKeys.PAGING, Map.of(
            ResponseKeys.PAGE, page,
            ResponseKeys.SIZE, size,
            ResponseKeys.TOTAL_ELEMENTS, filtered.size(),
            ResponseKeys.TOTAL_PAGES, totalPages));
  }

  /**
   * 指定エスカレーションの詳細を返す。
   * インプット: user 認証ユーザー、escalationId 対象ID。
   * アウトプット: エスカレーション詳細情報（履歴含む）。
   *
   * @param user          認証ユーザー
   * @param escalationId  エスカレーションID
   * @return 詳細結果
   */
  public Map<String, Object> detail(AuthUser user, String escalationId) {
    requireEscalationAccess(user);
    EscalationRecord esc = getVisibleEscalation(user, escalationId);

    List<Map<String, Object>> history = esc.getLogs().stream()
        .sorted(Comparator.comparing(EscalationLogRecord::getCreatedAt))
        .map(log -> {
          Map<String, Object> entry = new HashMap<>();
          entry.put(ResponseKeys.LOG_ID, log.getLogId());
          entry.put(ResponseKeys.LOG_DATE, log.getCreatedAt().toString());
          entry.put(ResponseKeys.AUTHOR_NAME, log.getAuthorName());
          entry.put(ResponseKeys.LOG_TEXT, log.getLogText());
          return entry;
        }).toList();

    Map<String, Object> params = new HashMap<>();
    params.put(ResponseKeys.ESCALATION_ID, esc.getEscalationId());
    params.put(ResponseKeys.ESC_TITLE, esc.getTitle());
    params.put(ResponseKeys.TARGET_EMPLOYEE_NAME, esc.getTargetEmployeeName());
    params.put(ResponseKeys.TARGET_TEAM, esc.getTargetTeam());
    params.put(ResponseKeys.DESCRIPTION, esc.getDescription());
    params.put(ResponseKeys.SEVERITY, esc.getSeverity());
    params.put(ResponseKeys.STATUS, esc.getStatus());
    params.put(ResponseKeys.CREATED_BY_NAME, esc.getCreatedByName());
    params.put(ResponseKeys.CREATED_BY_ROLE, esc.getCreatedByRole());
    params.put(ResponseKeys.ASSIGNEE_USER_ID, esc.getAssigneeUserId());
    params.put(ResponseKeys.HISTORY, history);
    params.put(ResponseKeys.UPDATED_AT, esc.getUpdatedAt().toString());
    return params;
  }

  /**
   * エスカレーションを新規作成する。
   * インプット: user 認証ユーザー、request 起票内容。
   * アウトプット: 作成したエスカレーションID。
   *
   * @param user    認証ユーザー
   * @param request 作成リクエスト
   * @return 作成結果
   */
  public Map<String, Object> create(AuthUser user, EscalationCreateRequest request) {
    // 起票は TL 以上のみ許可する。
    // 起票は TL 以上のみ許可する（NG・TM は不可）。
    if (!user.role().canAccessEscalation()) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.ESC_NO_CREATE_PERMISSION));
    }
    validateSeverity(request.severity());
    String initialStatus = (request.status() == null || request.status().isBlank())
        ? "PENDING" : request.status().toUpperCase(Locale.ROOT);
    validateStatus(initialStatus);

    EscalationRecord esc = new EscalationRecord();
    esc.setEscalationId(dataStore.newEscalationId());
    esc.setTitle(request.title());
    esc.setTargetEmployeeName(request.targetEmployeeName());
    esc.setTargetTeam(request.targetTeam());
    esc.setDescription(request.description());
    esc.setSeverity(request.severity().toUpperCase(Locale.ROOT));
    esc.setStatus(initialStatus);
    esc.setCreatedBy(user.userId());
    esc.setCreatedByName(user.name());
    esc.setCreatedByRole(user.role().name());
    esc.setAssigneeUserId(request.assigneeUserId());
    esc.setOfficeCode(user.officeCode());
    esc.setTeamCode(user.teamCode());
    esc.setCreatedAt(OffsetDateTime.now());
    esc.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveEscalation(esc);

    return Map.of(ResponseKeys.ESCALATION_ID, esc.getEscalationId());
  }

  /**
   * エスカレーションを更新する（内容変更・ステータス変更共用）。
   * インプット: user 認証ユーザー、request 更新内容。
   * アウトプット: 更新結果。
   *
   * @param user    認証ユーザー
   * @param request 更新リクエスト
   * @return 更新結果
   */
  public Map<String, Object> update(AuthUser user, EscalationUpdateRequest request) {
    requireEscalationAccess(user);
    EscalationRecord esc = getVisibleEscalation(user, request.escalationId());

    // 更新権限を確認する（起票者またはスコープ内管理ロール）。
    if (!canUpdate(user, esc)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.ESC_NO_UPDATE_PERMISSION));
    }
    validateSeverity(request.severity());
    validateStatus(request.status());

    esc.setTitle(request.title());
    esc.setTargetEmployeeName(request.targetEmployeeName());
    esc.setTargetTeam(request.targetTeam());
    esc.setDescription(request.description());
    esc.setSeverity(request.severity().toUpperCase(Locale.ROOT));
    esc.setStatus(request.status().toUpperCase(Locale.ROOT));
    esc.setAssigneeUserId(request.assigneeUserId());
    esc.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveEscalation(esc);

    return Map.of(ResponseKeys.ESCALATION_ID, esc.getEscalationId(), ResponseKeys.UPDATED, true);
  }

  /**
   * 対応ログを追記する（削除不可）。
   * インプット: user 認証ユーザー、request ログ内容。
   * アウトプット: ログ追加結果。
   *
   * @param user    認証ユーザー
   * @param request ログ追加リクエスト
   * @return ログ追加結果
   */
  public Map<String, Object> addLog(AuthUser user, EscalationLogAddRequest request) {
    requireEscalationAccess(user);
    EscalationRecord esc = getVisibleEscalation(user, request.escalationId());

    EscalationLogRecord log = new EscalationLogRecord();
    log.setLogId(dataStore.newLogId());
    log.setLogText(request.logText());
    log.setAuthorUserId(user.userId());
    log.setAuthorName(user.name());
    log.setCreatedAt(OffsetDateTime.now());

    esc.getLogs().add(log);
    esc.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveEscalation(esc);

    return Map.of(
        ResponseKeys.ESCALATION_ID, esc.getEscalationId(),
        ResponseKeys.LOG_ADDED, true,
        ResponseKeys.LOG_DATE, log.getCreatedAt().toString());
  }

  /**
   * ロール別アクセス可否を確認する（NG / TM は403）。
   *
   * @param user 認証ユーザー
   */
  private void requireEscalationAccess(AuthUser user) {
    if (!user.role().canAccessEscalation()) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.ESC_NO_ACCESS_PERMISSION));
    }
  }

  /**
   * ロール別スコープに絞ったエスカレーション一覧を返す。
   *
   * @param user 認証ユーザー
   * @return 参照可能なエスカレーション一覧
   */
  private List<EscalationRecord> scopedEscalations(AuthUser user) {
    return new ArrayList<>(dataStore.findAllEscalations()).stream()
        .filter(e -> canView(user, e))
        .toList();
  }

  /**
   * 可視なエスカレーション1件を取得し、不可視なら業務例外を送出する。
   *
   * @param user          認証ユーザー
   * @param escalationId  エスカレーションID
   * @return 可視なエスカレーション
   */
  private EscalationRecord getVisibleEscalation(AuthUser user, String escalationId) {
    EscalationRecord esc = dataStore.findEscalationById(escalationId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.ESC_404, msg(MessageKeys.ESC_NOT_FOUND)));
    if (!canView(user, esc)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.ESC_NO_ACCESS_PERMISSION));
    }
    return esc;
  }

  /**
   * ロールと所属に基づいてエスカレーション閲覧可否を判定する。
   *
   * @param user 認証ユーザー
   * @param esc  対象エスカレーション
   * @return 閲覧可否
   */
  private boolean canView(AuthUser user, EscalationRecord esc) {
    // SA/OM は全件参照可能（SA はフロントエンドで内容をマスク表示）。
    if (user.role() == UserRole.SA || user.role() == UserRole.OM) return true;
    // SM は同一オフィスの全件参照可能。
    if (user.role() == UserRole.SM) return user.officeCode().equals(esc.getOfficeCode());
    if (user.role() == UserRole.GL) return user.officeCode().equals(esc.getOfficeCode());
    // TL は自チームの案件 + 自分が起票した案件。
    if (user.role() == UserRole.TL) {
      return user.teamCode().equals(esc.getTeamCode()) || user.userId().equals(esc.getCreatedBy());
    }
    // SP: 担当者（assigneeUserId）が自分のエスカレーションのみ参照可能。
    if (user.role() == UserRole.SP) {
      return user.userId().equals(esc.getAssigneeUserId());
    }
    return false;
  }

  /**
   * エスカレーション更新権限を判定する。
   *
   * @param user 認証ユーザー
   * @param esc  対象エスカレーション
   * @return 更新可否
   */
  private boolean canUpdate(AuthUser user, EscalationRecord esc) {
    if (user.role() == UserRole.SA || user.role() == UserRole.SM
      || user.role() == UserRole.OM || user.role() == UserRole.GL) return true;
    // TL は起票者本人またはスコープ内（canView が true であれば更新も許可）。
    if (user.role() == UserRole.TL) return canView(user, esc);
    // SP は担当エスカレーション（canView が true であれば更新も許可）。
    if (user.role() == UserRole.SP) return canView(user, esc);
    return false;
  }

  /**
   * severity 値の許可値チェック。
   *
   * @param severity 重要度
   */
  private void validateSeverity(String severity) {
    if (severity == null || !VALID_SEVERITIES.contains(severity.toUpperCase(Locale.ROOT))) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.ESC_INVALID_SEVERITY));
    }
  }

  /**
   * status 値の許可値チェック。
   *
   * @param status ステータス
   */
  private void validateStatus(String status) {
    if (status == null || !VALID_STATUSES.contains(status.toUpperCase(Locale.ROOT))) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.ESC_INVALID_STATUS));
    }
  }

  /**
   * メッセージキーから日本語メッセージを取得する。
   *
   * @param key メッセージキー
   * @return メッセージ文字列
   */
  private String msg(String key) {
    return messageSource.getMessage(key, null, Locale.JAPANESE);
  }
}
