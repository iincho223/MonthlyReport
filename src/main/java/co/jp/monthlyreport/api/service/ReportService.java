package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.common.ValidationConstants;
import co.jp.monthlyreport.api.dto.request.FeedbackUpdateRequest;
import co.jp.monthlyreport.api.dto.request.ReportCreateRequest;
import co.jp.monthlyreport.api.dto.request.ReportSearchRequest;
import co.jp.monthlyreport.api.dto.request.ReportUpdateRequest;
import co.jp.monthlyreport.api.model.ReportRecord;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.InMemoryDataStore;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
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
 * 月報の検索・詳細・登録更新削除・回答更新を扱うサービス。
 * インプット: 認証ユーザー情報と各 API リクエスト。
 * アウトプット: 画面/API が利用する月報データまたは処理結果。
 */
public class ReportService {
  private static final Set<String> REQUIRED_CONDITION_KEYS = Set.of(
      "physical", "stress", "relationships", "worries", "fatigue", "sleep", "motivation");
  private static final Set<String> CONDITION_VALUES = Set.of("BEST", "GOOD", "WARN", "NG");

  private final InMemoryDataStore dataStore;
  private final MessageSource messageSource;

  public ReportService(InMemoryDataStore dataStore, MessageSource messageSource) {
    this.dataStore = dataStore;
    this.messageSource = messageSource;
  }

  /**
   * 権限スコープ内の月報を条件検索し、ページング結果を返す。
   * インプット: user 認証ユーザー、request 検索条件。
   * アウトプット: 月報一覧とページング情報のマップ。
   *
   * @param user 認証ユーザー
   * @param request 検索条件
   * @return 月報一覧とページング情報
   */
  public Map<String, Object> search(AuthUser user, ReportSearchRequest request) {
    int page = request.page() == null ? 1 : request.page();
    int size = request.size() == null ? 20 : request.size();
    String status = request.status() == null || request.status().isBlank() ? "ALL" : request.status().toUpperCase(Locale.ROOT);

    List<ReportRecord> filtered = scopedReports(user).stream()
        .filter(r -> request.month() == null || request.month().isBlank() || request.month().equals(r.getMonth()))
        .filter(r -> statusFilter(status, r))
        .sorted(Comparator.comparing(ReportRecord::getMonth).reversed().thenComparing(ReportRecord::getUpdatedAt, Comparator.reverseOrder()))
        .collect(Collectors.toList());

    int from = Math.min((page - 1) * size, filtered.size());
    int to = Math.min(from + size, filtered.size());

    List<Map<String, Object>> items = filtered.subList(from, to).stream().map(r -> {
      Map<String, Object> item = new HashMap<>();
      item.put(ResponseKeys.REPORT_ID, r.getReportId());
      item.put(ResponseKeys.MONTH, r.getMonth());
      item.put(ResponseKeys.REPORTER_NAME, r.getReporterName());
      item.put(ResponseKeys.REPORTER_ID, r.getReporterId());
      item.put(ResponseKeys.AUTHOR_ROLE, r.getAuthorRole().name());
      item.put(ResponseKeys.OFFICE_CODE, r.getOfficeCode());
      item.put(ResponseKeys.TEAM_CODE, r.getTeamCode());
      item.put(ResponseKeys.FEEDBACK_REGISTERED, r.hasFeedback());
      item.put(ResponseKeys.UPDATED_AT, r.getUpdatedAt().toString());
      return item;
    }).toList();

    int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

    // フロント仕様に合わせ、items と paging を同時に返す。
    return Map.of(
        ResponseKeys.ITEMS, items,
        ResponseKeys.PAGING, Map.of(
            ResponseKeys.PAGE, page,
            ResponseKeys.SIZE, size,
            ResponseKeys.TOTAL_ELEMENTS, filtered.size(),
            ResponseKeys.TOTAL_PAGES, totalPages));
  }

  /**
   * 指定月報の詳細を返す。
   * インプット: user 認証ユーザー、reportId 月報ID。
   * アウトプット: 月報詳細情報のマップ。
   *
   * @param user 認証ユーザー
   * @param reportId 月報ID
   * @return 月報詳細
   */
  public Map<String, Object> detail(AuthUser user, String reportId) {
    // 参照権限を含めて可視な月報のみ取得する。
    ReportRecord report = getVisibleReport(user, reportId);
    Map<String, Object> author = Map.of(
        ResponseKeys.EMPLOYEE_NO, report.getReporterId(),
        ResponseKeys.NAME, report.getReporterName(),
        ResponseKeys.ROLE, report.getAuthorRole().name(),
        ResponseKeys.OFFICE_CODE, report.getOfficeCode(),
        ResponseKeys.TEAM_CODE, report.getTeamCode());

    Map<String, Object> feedback = new HashMap<>();
    feedback.put(ResponseKeys.FEEDBACK_COMMENT, report.getFeedbackComment());
    feedback.put(ResponseKeys.RESPONDER_ROLE, report.getResponderRole());
    feedback.put(ResponseKeys.RESPONDER_NAME, report.getResponderName());
    feedback.put(ResponseKeys.RESPONDED_AT, report.getRespondedAt() == null ? null : report.getRespondedAt().toString());

    Map<String, Object> params = new HashMap<>();
    params.put(ResponseKeys.REPORT_ID, report.getReportId());
    params.put(ResponseKeys.MONTH, report.getMonth());
    params.put(ResponseKeys.SALES_INFO, report.getSalesInfo());
    params.put(ResponseKeys.NEXT_MONTH_OVERTIME_HOURS, report.getNextMonthOvertimeHours());
    params.put(ResponseKeys.NEXT_MONTH_OVERTIME_REASON, report.getNextMonthOvertimeReason());
    params.put(ResponseKeys.THIS_MONTH_OVERTIME_HOURS, report.getThisMonthOvertimeHours());
    params.put(ResponseKeys.THIS_MONTH_OVERTIME_REASON, report.getThisMonthOvertimeReason());
    params.put(ResponseKeys.CONDITIONS, report.getConditions());
    params.put(ResponseKeys.COMMENTS, report.getComments());
    params.put(ResponseKeys.AUTHOR, author);
    params.put(ResponseKeys.FEEDBACK, feedback);
    params.put(ResponseKeys.UPDATED_AT, report.getUpdatedAt().toString());
    return params;
  }

  /**
   * 月報を新規作成する。
   * インプット: user 認証ユーザー、request 作成内容。
   * アウトプット: 作成した月報ID。
   *
   * @param user 認証ユーザー
   * @param request 月報作成リクエスト
   * @return 作成結果
   */
  public Map<String, Object> create(AuthUser user, ReportCreateRequest request) {
    // NG は月報の提出不可。
    if (user.role() == UserRole.NG) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_CREATE_PERMISSION));
    }
    // 体調コンディションのキー/値を事前検証する。
    validateConditions(request.conditions());

    boolean duplicated = dataStore.findAllReports().stream()
        .filter(r -> !r.isDeleted())
        .anyMatch(r -> r.getAuthorUserId().equals(user.userId()) && r.getMonth().equals(request.month()));
    // 同一ユーザー・同一月の重複投稿を防ぐ。
    if (duplicated) {
      throw new BusinessException(ErrorCodes.REPORT_409, msg(MessageKeys.REPORT_DUPLICATE_MONTH));
    }

    ReportRecord report = new ReportRecord();
    report.setReportId(dataStore.newReportId());
    applyEditableFields(report, request.month(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.conditions(), request.comments());
    report.setAuthorUserId(user.userId());
    report.setReporterId(user.employeeNo());
    report.setReporterName(user.name());
    report.setAuthorRole(user.role());
    report.setOfficeCode(user.officeCode());
    report.setTeamCode(user.teamCode());
    report.setCreatedAt(OffsetDateTime.now());
    report.setUpdatedAt(OffsetDateTime.now());
    report.setDeleted(false);
    // 永続化ストアへ保存する。
    dataStore.saveReport(report);

    return Map.of(ResponseKeys.REPORT_ID, report.getReportId());
  }

  /**
   * 月報を更新する。
   * インプット: user 認証ユーザー、request 更新内容。
   * アウトプット: 更新結果。
   *
   * @param user 認証ユーザー
   * @param request 月報更新リクエスト
   * @return 更新結果
   */
  public Map<String, Object> update(AuthUser user, ReportUpdateRequest request) {
    validateConditions(request.conditions());
    ReportRecord report = dataStore.findReportById(request.reportId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    // 作成者本人のみ更新を許可する。
    if (!report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_UPDATE_PERMISSION));
    }

    applyEditableFields(report, request.month(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.conditions(), request.comments());
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);
    return Map.of(ResponseKeys.REPORT_ID, report.getReportId(), ResponseKeys.UPDATED, true);
  }

  /**
   * 月報を論理削除する。
   * インプット: user 認証ユーザー、reportId 月報ID。
   * アウトプット: 削除結果。
   *
   * @param user 認証ユーザー
   * @param reportId 月報ID
   * @return 削除結果
   */
  public Map<String, Object> delete(AuthUser user, String reportId) {
    ReportRecord report = dataStore.findReportById(reportId)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    boolean canDelete = report.getAuthorUserId().equals(user.userId()) || user.role() == UserRole.OM;
    // 投稿者本人または OM のみ削除可能。
    if (!canDelete) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_DELETE_PERMISSION));
    }

    report.setDeleted(true);
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);
    return Map.of(ResponseKeys.REPORT_ID, report.getReportId(), ResponseKeys.DELETED, true);
  }

  /**
   * 月報への回答を更新する。
   * インプット: user 認証ユーザー、request 回答内容。
   * アウトプット: 回答更新結果。
   *
   * @param user 認証ユーザー
   * @param request 回答更新リクエスト
   * @return 回答更新結果
   */
  public Map<String, Object> updateFeedback(AuthUser user, FeedbackUpdateRequest request) {
    // 回答権限ロールのみ許可する。
    if (!user.role().canRespond()) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_FEEDBACK_PERMISSION));
    }

    ReportRecord report = dataStore.findReportById(request.reportId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    // 自分自身の月報への回答は禁止。
    if (report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_FEEDBACK_SELF));
    }

    // フィードバック期限チェック: 報告月の翌月 DEADLINE_DAY 日を過ぎた場合は不可。
    LocalDate deadline = YearMonth.parse(report.getMonth())
        .plusMonths(1)
        .atDay(ValidationConstants.DEADLINE_DAY);
    if (LocalDate.now().isAfter(deadline)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_FEEDBACK_EXPIRED));
    }

    report.setFeedbackComment(request.feedbackComment());
    report.setResponderRole(user.role().name());
    report.setResponderName(user.name());
    report.setRespondedAt(OffsetDateTime.now());
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);

    return Map.of(
        ResponseKeys.REPORT_ID, report.getReportId(),
        ResponseKeys.FEEDBACK_REGISTERED, true,
        ResponseKeys.RESPONDER_ROLE, report.getResponderRole(),
        ResponseKeys.RESPONDER_NAME, report.getResponderName(),
        ResponseKeys.RESPONDED_AT, report.getRespondedAt().toString());
  }

  /**
   * ログインユーザーの参照範囲に絞った月報一覧を返す。
   * インプット: user 認証ユーザー。
   * アウトプット: 参照可能な月報一覧。
   *
   * @param user 認証ユーザー
   * @return 参照可能な月報一覧
   */
  public List<ReportRecord> scopedReports(AuthUser user) {
    List<ReportRecord> records = new ArrayList<>(dataStore.findAllReports()).stream()
        .filter(r -> !r.isDeleted())
        .toList();

    return records.stream().filter(r -> canView(user, r)).toList();
  }

  /**
   * 可視な月報 1 件を取得し、不可視なら業務例外を送出する。
   * インプット: user 認証ユーザー、reportId 月報ID。
   * アウトプット: 可視判定済みの月報。
   *
   * @param user 認証ユーザー
   * @param reportId 月報ID
   * @return 可視な月報
   */
  private ReportRecord getVisibleReport(AuthUser user, String reportId) {
    ReportRecord report = dataStore.findReportById(reportId)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));
    // 取得できても閲覧権限がなければ拒否する。
    if (!canView(user, report)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_VIEW_PERMISSION));
    }
    return report;
  }

  /**
   * ロールと所属に基づいて閲覧可否を判定する。
   * インプット: user 認証ユーザー、report 対象月報。
   * アウトプット: 閲覧可能な場合 true。
   *
   * @param user 認証ユーザー
   * @param report 対象月報
   * @return 閲覧可否
   */
  private boolean canView(AuthUser user, ReportRecord report) {
    // NG はすべての月報を閲覧不可。
    if (user.role() == UserRole.NG) {
      return false;
    }
    // SA/OM は全件参照可能（SA はフロントエンドで内容をマスク表示）。
    if (user.role() == UserRole.SA || user.role() == UserRole.OM) {
      return true;
    }
    // SM は同一オフィスの全件参照可能。
    if (user.role() == UserRole.SM) {
      return report.getOfficeCode().equals(user.officeCode());
    }
    // GL は同一オフィスまたは本人投稿のみ参照可能。
    if (user.role() == UserRole.GL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getOfficeCode().equals(user.officeCode());
    }
    // TL は同一チームまたは本人投稿のみ参照可能。
    if (user.role() == UserRole.TL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getTeamCode().equals(user.teamCode());
    }
    // TM/SP は本人投稿のみ参照可能。
    return report.getAuthorUserId().equals(user.userId());
  }

  /**
   * 状態フィルタ条件を評価する。
   * インプット: status 状態条件、report 対象月報。
   * アウトプット: 条件一致時 true。
   *
   * @param status 状態条件
   * @param report 対象月報
   * @return 条件一致可否
   */
  private boolean statusFilter(String status, ReportRecord report) {
    return switch (status) {
      case "ALL", "SUBMITTED" -> true;
      case "PENDING_FEEDBACK" -> !report.hasFeedback();
      case "FEEDBACKED" -> report.hasFeedback();
      default -> throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.REPORT_INVALID_STATUS));
    };
  }

  /**
   * 編集可能項目を一括反映する。
   * インプット: report 更新対象月報と編集対象の各入力値。
   * アウトプット: report の編集可能項目が更新された状態。
   *
   * @param report 更新対象月報
   * @param month 対象月
   * @param salesInfo 売上情報
   * @param nextMonthOvertimeHours 来月見込み残業時間
   * @param nextMonthOvertimeReason 来月見込み残業理由
   * @param thisMonthOvertimeHours 今月実績残業時間
   * @param thisMonthOvertimeReason 今月実績残業理由
   * @param conditions 体調コンディション
   * @param comments コメント
   */
  private void applyEditableFields(
      ReportRecord report,
      String month,
      String salesInfo,
      Integer nextMonthOvertimeHours,
      String nextMonthOvertimeReason,
      Integer thisMonthOvertimeHours,
      String thisMonthOvertimeReason,
      Map<String, String> conditions,
      String comments) {
    report.setMonth(month);
    report.setSalesInfo(salesInfo);
    report.setNextMonthOvertimeHours(nextMonthOvertimeHours);
    report.setNextMonthOvertimeReason(nextMonthOvertimeReason);
    report.setThisMonthOvertimeHours(thisMonthOvertimeHours);
    report.setThisMonthOvertimeReason(thisMonthOvertimeReason);
    report.setConditions(new HashMap<>(conditions));
    report.setComments(comments);
  }

  /**
   * メッセージキーから日本語メッセージを取得する。
   * インプット: key メッセージキー。
   * アウトプット: messages.properties から取得したメッセージ文字列。
   *
   * @param key メッセージキー
   * @return メッセージ文字列
   */
  private String msg(String key) {
    return messageSource.getMessage(key, null, Locale.JAPANESE);
  }

  /**
   * 体調コンディションの必須キーと許可値を検証する。
   * インプット: conditions 体調コンディション。
   * アウトプット: 不正時は業務例外、正常時は処理継続。
   *
   * @param conditions 体調コンディション
   */
  private void validateConditions(Map<String, String> conditions) {
    // 必須キーが 1 つでも欠けていれば不正。
    if (!conditions.keySet().containsAll(REQUIRED_CONDITION_KEYS)) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.REPORT_CONDITIONS_MISSING_KEY));
    }
    // キー/値の組み合わせが許可範囲外なら不正。
    conditions.forEach((k, v) -> {
      if (!REQUIRED_CONDITION_KEYS.contains(k) || !CONDITION_VALUES.contains(v)) {
        throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.REPORT_CONDITIONS_INVALID_VALUE));
      }
    });
  }
}
