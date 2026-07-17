package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.common.UlidGenerator;
import co.jp.monthlyreport.api.common.ValidationConstants;
import co.jp.monthlyreport.api.dto.request.FeedbackUpdateRequest;
import co.jp.monthlyreport.api.dto.request.ReportCreateRequest;
import co.jp.monthlyreport.api.dto.request.ReportSearchRequest;
import co.jp.monthlyreport.api.dto.request.ReportUpdateRequest;
import co.jp.monthlyreport.api.entity.ReportConditionEntity;
import co.jp.monthlyreport.api.entity.ReportEntity;
import co.jp.monthlyreport.api.entity.ReportFeedbackEntity;
import co.jp.monthlyreport.api.entity.UserEntity;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.ReportConditionRepository;
import co.jp.monthlyreport.api.repository.ReportFeedbackRepository;
import co.jp.monthlyreport.api.repository.ReportRepository;
import co.jp.monthlyreport.api.repository.ReportSpecifications;
import co.jp.monthlyreport.api.repository.UserRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
  private static final Set<String> VALID_STATUS_FILTERS = Set.of("ALL", "SUBMITTED", "PENDING_FEEDBACK", "FEEDBACKED");
  private static final String STATUS_FEEDBACKED = "FEEDBACKED";
  private static final String STATUS_SUBMITTED = "SUBMITTED";

  private final ReportRepository reportRepository;
  private final ReportConditionRepository reportConditionRepository;
  private final ReportFeedbackRepository reportFeedbackRepository;
  private final UserRepository userRepository;
  private final MessageSource messageSource;

  public ReportService(ReportRepository reportRepository, ReportConditionRepository reportConditionRepository,
      ReportFeedbackRepository reportFeedbackRepository, UserRepository userRepository, MessageSource messageSource) {
    this.reportRepository = reportRepository;
    this.reportConditionRepository = reportConditionRepository;
    this.reportFeedbackRepository = reportFeedbackRepository;
    this.userRepository = userRepository;
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
    if (!VALID_STATUS_FILTERS.contains(status)) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.REPORT_INVALID_STATUS));
    }

    var spec = ReportSpecifications.notDeleted()
        .and(ReportSpecifications.visibleTo(user))
        .and(ReportSpecifications.month(request.month()))
        .and(ReportSpecifications.status(status));
    Sort sort = Sort.by(Sort.Direction.DESC, "reportMonth").and(Sort.by(Sort.Direction.DESC, "updatedAt"));
    Page<ReportEntity> pageResult = reportRepository.findAll(spec, PageRequest.of(page - 1, size, sort));

    List<Map<String, Object>> items = pageResult.getContent().stream().map(r -> {
      UserEntity author = userRepository.findById(r.getAuthorUserId()).orElse(null);
      Map<String, Object> item = new HashMap<>();
      item.put(ResponseKeys.REPORT_ID, r.getReportId());
      item.put(ResponseKeys.MONTH, r.getReportMonth());
      item.put(ResponseKeys.REPORTER_NAME, author == null ? null : author.getUserName());
      item.put(ResponseKeys.REPORTER_ID, author == null ? null : author.getEmployeeNo());
      item.put(ResponseKeys.AUTHOR_ROLE, author == null ? null : author.getRoleCode());
      item.put(ResponseKeys.OFFICE_CODE, r.getOfficeCode());
      item.put(ResponseKeys.TEAM_CODE, r.getTeamCode());
      item.put(ResponseKeys.FEEDBACK_REGISTERED, STATUS_FEEDBACKED.equals(r.getStatus()));
      item.put(ResponseKeys.UPDATED_AT, r.getUpdatedAt().toString());
      return item;
    }).toList();

    // フロント仕様に合わせ、items と paging を同時に返す。
    return Map.of(
        ResponseKeys.ITEMS, items,
        ResponseKeys.PAGING, Map.of(
            ResponseKeys.PAGE, page,
            ResponseKeys.SIZE, size,
            ResponseKeys.TOTAL_ELEMENTS, pageResult.getTotalElements(),
            ResponseKeys.TOTAL_PAGES, pageResult.getTotalPages()));
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
    ReportEntity report = getVisibleReport(user, reportId);
    UserEntity authorUser = userRepository.findById(report.getAuthorUserId()).orElse(null);
    Map<String, Object> author = new HashMap<>();
    author.put(ResponseKeys.EMPLOYEE_NO, authorUser == null ? null : authorUser.getEmployeeNo());
    author.put(ResponseKeys.NAME, authorUser == null ? null : authorUser.getUserName());
    author.put(ResponseKeys.ROLE, authorUser == null ? null : authorUser.getRoleCode());
    author.put(ResponseKeys.OFFICE_CODE, report.getOfficeCode());
    author.put(ResponseKeys.TEAM_CODE, report.getTeamCode());

    ReportFeedbackEntity feedbackEntity = reportFeedbackRepository.findById(reportId).orElse(null);
    Map<String, Object> feedback = new HashMap<>();
    if (feedbackEntity != null) {
      UserEntity responder = userRepository.findById(feedbackEntity.getResponderUserId()).orElse(null);
      feedback.put(ResponseKeys.FEEDBACK_COMMENT, feedbackEntity.getFeedbackComment());
      feedback.put(ResponseKeys.RESPONDER_ROLE, responder == null ? null : responder.getRoleCode());
      feedback.put(ResponseKeys.RESPONDER_NAME, responder == null ? null : responder.getUserName());
      feedback.put(ResponseKeys.RESPONDED_AT, feedbackEntity.getRespondedAt().toString());
    } else {
      feedback.put(ResponseKeys.FEEDBACK_COMMENT, null);
      feedback.put(ResponseKeys.RESPONDER_ROLE, null);
      feedback.put(ResponseKeys.RESPONDER_NAME, null);
      feedback.put(ResponseKeys.RESPONDED_AT, null);
    }

    ReportConditionEntity condition = reportConditionRepository.findById(reportId).orElseThrow();
    Map<String, String> conditions = Map.of(
        "physical", condition.getPhysical(),
        "stress", condition.getStress(),
        "relationships", condition.getRelationships(),
        "worries", condition.getWorries(),
        "fatigue", condition.getFatigue(),
        "sleep", condition.getSleep(),
        "motivation", condition.getMotivation());

    Map<String, Object> params = new HashMap<>();
    params.put(ResponseKeys.REPORT_ID, report.getReportId());
    params.put(ResponseKeys.MONTH, report.getReportMonth());
    params.put(ResponseKeys.SALES_INFO, report.getSalesInfo());
    params.put(ResponseKeys.NEXT_MONTH_OVERTIME_HOURS, report.getNextMonthOvertimeHours());
    params.put(ResponseKeys.NEXT_MONTH_OVERTIME_REASON, report.getNextMonthOvertimeReason());
    params.put(ResponseKeys.THIS_MONTH_OVERTIME_HOURS, report.getThisMonthOvertimeHours());
    params.put(ResponseKeys.THIS_MONTH_OVERTIME_REASON, report.getThisMonthOvertimeReason());
    params.put(ResponseKeys.CONDITIONS, conditions);
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

    // 同一ユーザー・同一月の重複投稿を防ぐ。
    if (reportRepository.existsByAuthorUserIdAndReportMonthAndDeleteFlagFalse(user.userId(), request.month())) {
      throw new BusinessException(ErrorCodes.REPORT_409, msg(MessageKeys.REPORT_DUPLICATE_MONTH));
    }

    OffsetDateTime now = OffsetDateTime.now();
    ReportEntity report = new ReportEntity();
    report.setReportId(UlidGenerator.generate());
    applyEditableFields(report, request.month(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.comments());
    report.setStatus(STATUS_SUBMITTED);
    report.setAuthorUserId(user.userId());
    report.setOfficeCode(user.officeCode());
    report.setTeamCode(user.teamCode());
    report.setDeleteFlag(false);
    report.setUpdatedAt(now);
    report.setUpdatedBy(user.employeeNo());
    report.setRegisteredAt(now);
    report.setRegisteredBy(user.employeeNo());
    reportRepository.save(report);

    saveConditions(report.getReportId(), request.conditions(), user, now);

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
    ReportEntity report = reportRepository.findByReportIdAndDeleteFlagFalse(request.reportId())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    // 作成者本人のみ更新を許可する。
    if (!report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_UPDATE_PERMISSION));
    }

    OffsetDateTime now = OffsetDateTime.now();
    applyEditableFields(report, request.month(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.comments());
    report.setUpdatedAt(now);
    report.setUpdatedBy(user.employeeNo());
    reportRepository.save(report);

    saveConditions(report.getReportId(), request.conditions(), user, now);

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
    ReportEntity report = reportRepository.findByReportIdAndDeleteFlagFalse(reportId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    boolean canDelete = report.getAuthorUserId().equals(user.userId()) || user.role() == UserRole.OM;
    // 投稿者本人または OM のみ削除可能。
    if (!canDelete) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_DELETE_PERMISSION));
    }

    report.setDeleteFlag(true);
    report.setUpdatedAt(OffsetDateTime.now());
    report.setUpdatedBy(user.employeeNo());
    reportRepository.save(report);
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

    ReportEntity report = reportRepository.findByReportIdAndDeleteFlagFalse(request.reportId())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));

    // 自分自身の月報への回答は禁止。
    if (report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_FEEDBACK_SELF));
    }

    // フィードバック期限チェック: 報告月の翌月 DEADLINE_DAY 日を過ぎた場合は不可。
    LocalDate deadline = YearMonth.parse(report.getReportMonth())
        .plusMonths(1)
        .atDay(ValidationConstants.DEADLINE_DAY);
    if (LocalDate.now().isAfter(deadline)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_FEEDBACK_EXPIRED));
    }

    OffsetDateTime now = OffsetDateTime.now();
    ReportFeedbackEntity feedback = reportFeedbackRepository.findById(request.reportId()).orElseGet(ReportFeedbackEntity::new);
    boolean isNew = feedback.getReportId() == null;
    feedback.setReportId(request.reportId());
    feedback.setFeedbackComment(request.feedbackComment());
    feedback.setResponderUserId(user.userId());
    feedback.setRespondedAt(now);
    feedback.setUpdatedAt(now);
    feedback.setUpdatedBy(user.employeeNo());
    if (isNew) {
      feedback.setRegisteredAt(now);
      feedback.setRegisteredBy(user.employeeNo());
    }
    feedback.setDeleteFlag(false);
    reportFeedbackRepository.save(feedback);

    report.setStatus(STATUS_FEEDBACKED);
    report.setUpdatedAt(now);
    report.setUpdatedBy(user.employeeNo());
    reportRepository.save(report);

    return Map.of(
        ResponseKeys.REPORT_ID, report.getReportId(),
        ResponseKeys.FEEDBACK_REGISTERED, true,
        ResponseKeys.RESPONDER_ROLE, user.role().name(),
        ResponseKeys.RESPONDER_NAME, user.name(),
        ResponseKeys.RESPONDED_AT, now.toString());
  }

  /**
   * ログインユーザーの参照範囲に絞った月報一覧を返す。
   * インプット: user 認証ユーザー。
   * アウトプット: 参照可能な月報一覧。
   *
   * @param user 認証ユーザー
   * @return 参照可能な月報一覧
   */
  public List<ReportEntity> scopedReports(AuthUser user) {
    var spec = ReportSpecifications.notDeleted().and(ReportSpecifications.visibleTo(user));
    return reportRepository.findAll(spec);
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
  private ReportEntity getVisibleReport(AuthUser user, String reportId) {
    ReportEntity report = reportRepository.findByReportIdAndDeleteFlagFalse(reportId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, msg(MessageKeys.REPORT_NOT_FOUND)));
    // 取得できても閲覧権限がなければ拒否する。
    if (!canView(user, report)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.REPORT_NO_VIEW_PERMISSION));
    }
    return report;
  }

  /**
   * ロールと所属に基づいて閲覧可否を判定する（{@link ReportSpecifications#visibleTo} と同一の判定）。
   *
   * @param user 認証ユーザー
   * @param report 対象月報
   * @return 閲覧可否
   */
  private boolean canView(AuthUser user, ReportEntity report) {
    if (user.role() == UserRole.NG) {
      return false;
    }
    if (user.role() == UserRole.SA || user.role() == UserRole.OM) {
      return true;
    }
    if (user.role() == UserRole.SM) {
      return report.getOfficeCode().equals(user.officeCode());
    }
    if (user.role() == UserRole.GL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getOfficeCode().equals(user.officeCode());
    }
    if (user.role() == UserRole.TL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getTeamCode().equals(user.teamCode());
    }
    return report.getAuthorUserId().equals(user.userId());
  }

  /**
   * 編集可能項目を一括反映する。
   *
   * @param report 更新対象月報
   * @param month 対象月
   * @param salesInfo 売上情報
   * @param nextMonthOvertimeHours 来月見込み残業時間
   * @param nextMonthOvertimeReason 来月見込み残業理由
   * @param thisMonthOvertimeHours 今月実績残業時間
   * @param thisMonthOvertimeReason 今月実績残業理由
   * @param comments コメント
   */
  private void applyEditableFields(
      ReportEntity report,
      String month,
      String salesInfo,
      Integer nextMonthOvertimeHours,
      String nextMonthOvertimeReason,
      Integer thisMonthOvertimeHours,
      String thisMonthOvertimeReason,
      String comments) {
    report.setReportMonth(month);
    report.setSalesInfo(salesInfo);
    report.setNextMonthOvertimeHours(nextMonthOvertimeHours);
    report.setNextMonthOvertimeReason(nextMonthOvertimeReason);
    report.setThisMonthOvertimeHours(thisMonthOvertimeHours);
    report.setThisMonthOvertimeReason(thisMonthOvertimeReason);
    report.setComments(comments);
  }

  /**
   * 体調コンディションを作成または更新する（reports と 1:1）。
   *
   * @param reportId   月報ID
   * @param conditions 体調コンディション
   * @param user       操作ユーザー
   * @param now        処理時刻
   */
  private void saveConditions(String reportId, Map<String, String> conditions, AuthUser user, OffsetDateTime now) {
    ReportConditionEntity entity = reportConditionRepository.findById(reportId).orElseGet(ReportConditionEntity::new);
    boolean isNew = entity.getReportId() == null;
    entity.setReportId(reportId);
    entity.setPhysical(conditions.get("physical"));
    entity.setStress(conditions.get("stress"));
    entity.setRelationships(conditions.get("relationships"));
    entity.setWorries(conditions.get("worries"));
    entity.setFatigue(conditions.get("fatigue"));
    entity.setSleep(conditions.get("sleep"));
    entity.setMotivation(conditions.get("motivation"));
    entity.setDeleteFlag(false);
    entity.setUpdatedAt(now);
    entity.setUpdatedBy(user.employeeNo());
    if (isNew) {
      entity.setRegisteredAt(now);
      entity.setRegisteredBy(user.employeeNo());
    }
    reportConditionRepository.save(entity);
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
