package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.dto.request.FeedbackUpdateRequest;
import co.jp.monthlyreport.api.dto.request.ReportCreateRequest;
import co.jp.monthlyreport.api.dto.request.ReportSearchRequest;
import co.jp.monthlyreport.api.dto.request.ReportUpdateRequest;
import co.jp.monthlyreport.api.model.ReportRecord;
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
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private static final Set<String> REQUIRED_CONDITION_KEYS = Set.of(
      "physical", "stress", "relationships", "worries", "fatigue", "sleep", "motivation");
  private static final Set<String> CONDITION_VALUES = Set.of("OK", "WARN", "NG");

  private final InMemoryDataStore dataStore;

  public ReportService(InMemoryDataStore dataStore) {
    this.dataStore = dataStore;
  }

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
      item.put("reportId", r.getReportId());
      item.put("month", r.getMonth());
      item.put("title", r.getTitle());
      item.put("reporterName", r.getReporterName());
      item.put("reporterId", r.getReporterId());
      item.put("authorRole", r.getAuthorRole().name());
      item.put("officeCode", r.getOfficeCode());
      item.put("teamCode", r.getTeamCode());
      item.put("feedbackRegistered", r.hasFeedback());
      item.put("updatedAt", r.getUpdatedAt().toString());
      return item;
    }).toList();

    int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

    return Map.of(
        "items", items,
        "paging", Map.of(
            "page", page,
            "size", size,
            "totalElements", filtered.size(),
            "totalPages", totalPages));
  }

  public Map<String, Object> detail(AuthUser user, String reportId) {
    ReportRecord report = getVisibleReport(user, reportId);
    Map<String, Object> author = Map.of(
        "employeeNo", report.getReporterId(),
        "name", report.getReporterName(),
        "role", report.getAuthorRole().name(),
        "officeCode", report.getOfficeCode(),
        "teamCode", report.getTeamCode());

    Map<String, Object> feedback = new HashMap<>();
    feedback.put("feedbackComment", report.getFeedbackComment());
    feedback.put("responderRole", report.getResponderRole());
    feedback.put("responderName", report.getResponderName());
    feedback.put("respondedAt", report.getRespondedAt() == null ? null : report.getRespondedAt().toString());

    Map<String, Object> params = new HashMap<>();
    params.put("reportId", report.getReportId());
    params.put("month", report.getMonth());
    params.put("title", report.getTitle());
    params.put("salesInfo", report.getSalesInfo());
    params.put("nextMonthOvertimeHours", report.getNextMonthOvertimeHours());
    params.put("nextMonthOvertimeReason", report.getNextMonthOvertimeReason());
    params.put("thisMonthOvertimeHours", report.getThisMonthOvertimeHours());
    params.put("thisMonthOvertimeReason", report.getThisMonthOvertimeReason());
    params.put("conditions", report.getConditions());
    params.put("comments", report.getComments());
    params.put("author", author);
    params.put("feedback", feedback);
    params.put("updatedAt", report.getUpdatedAt().toString());
    return params;
  }

  public Map<String, Object> create(AuthUser user, ReportCreateRequest request) {
    validateConditions(request.conditions());

    boolean duplicated = dataStore.findAllReports().stream()
        .filter(r -> !r.isDeleted())
        .anyMatch(r -> r.getAuthorUserId().equals(user.userId()) && r.getMonth().equals(request.month()));
    if (duplicated) {
      throw new BusinessException(ErrorCodes.REPORT_409, "同一月の月報は既に存在します");
    }

    ReportRecord report = new ReportRecord();
    report.setReportId(dataStore.newReportId());
    applyEditableFields(report, request.month(), request.title(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.conditions(), request.comments());
    report.setAuthorUserId(user.userId());
    report.setReporterId(user.employeeNo());
    report.setReporterName(user.name());
    report.setAuthorRole(user.role());
    report.setOfficeCode(user.officeCode());
    report.setTeamCode(user.teamCode());
    report.setCreatedAt(OffsetDateTime.now());
    report.setUpdatedAt(OffsetDateTime.now());
    report.setDeleted(false);
    dataStore.saveReport(report);

    return Map.of("reportId", report.getReportId());
  }

  public Map<String, Object> update(AuthUser user, ReportUpdateRequest request) {
    validateConditions(request.conditions());
    ReportRecord report = dataStore.findReportById(request.reportId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, "月報が見つかりません"));

    if (!report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, "更新権限がありません");
    }

    applyEditableFields(report, request.month(), request.title(), request.salesInfo(), request.nextMonthOvertimeHours(), request.nextMonthOvertimeReason(), request.thisMonthOvertimeHours(), request.thisMonthOvertimeReason(), request.conditions(), request.comments());
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);
    return Map.of("reportId", report.getReportId(), "updated", true);
  }

  public Map<String, Object> delete(AuthUser user, String reportId) {
    ReportRecord report = dataStore.findReportById(reportId)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, "月報が見つかりません"));

    boolean canDelete = report.getAuthorUserId().equals(user.userId()) || user.role() == UserRole.OM;
    if (!canDelete) {
      throw new BusinessException(ErrorCodes.AUTH_403, "削除権限がありません");
    }

    report.setDeleted(true);
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);
    return Map.of("reportId", report.getReportId(), "deleted", true);
  }

  public Map<String, Object> updateFeedback(AuthUser user, FeedbackUpdateRequest request) {
    if (!user.role().canRespond()) {
      throw new BusinessException(ErrorCodes.AUTH_403, "回答権限がありません");
    }

    ReportRecord report = dataStore.findReportById(request.reportId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, "月報が見つかりません"));

    if (report.getAuthorUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.AUTH_403, "自分の月報には回答できません");
    }

    report.setFeedbackComment(request.feedbackComment());
    report.setResponderRole(user.role().name());
    report.setResponderName(user.name());
    report.setRespondedAt(OffsetDateTime.now());
    report.setUpdatedAt(OffsetDateTime.now());
    dataStore.saveReport(report);

    return Map.of(
        "reportId", report.getReportId(),
        "feedbackRegistered", true,
        "responderRole", report.getResponderRole(),
        "responderName", report.getResponderName(),
        "respondedAt", report.getRespondedAt().toString());
  }

  public List<ReportRecord> scopedReports(AuthUser user) {
    List<ReportRecord> records = new ArrayList<>(dataStore.findAllReports()).stream()
        .filter(r -> !r.isDeleted())
        .toList();

    return records.stream().filter(r -> canView(user, r)).toList();
  }

  private ReportRecord getVisibleReport(AuthUser user, String reportId) {
    ReportRecord report = dataStore.findReportById(reportId)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.REPORT_404, "月報が見つかりません"));
    if (!canView(user, report)) {
      throw new BusinessException(ErrorCodes.AUTH_403, "参照権限がありません");
    }
    return report;
  }

  private boolean canView(AuthUser user, ReportRecord report) {
    if (user.role() == UserRole.OM) {
      return true;
    }
    if (user.role() == UserRole.GL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getOfficeCode().equals(user.officeCode());
    }
    if (user.role() == UserRole.TL) {
      return report.getAuthorUserId().equals(user.userId()) || report.getTeamCode().equals(user.teamCode());
    }
    return report.getAuthorUserId().equals(user.userId());
  }

  private boolean statusFilter(String status, ReportRecord report) {
    return switch (status) {
      case "ALL", "SUBMITTED" -> true;
      case "PENDING_FEEDBACK" -> !report.hasFeedback();
      case "FEEDBACKED" -> report.hasFeedback();
      default -> throw new BusinessException(ErrorCodes.VAL_001, "status が不正です");
    };
  }

  private void applyEditableFields(
      ReportRecord report,
      String month,
      String title,
      String salesInfo,
      Integer nextMonthOvertimeHours,
      String nextMonthOvertimeReason,
      Integer thisMonthOvertimeHours,
      String thisMonthOvertimeReason,
      Map<String, String> conditions,
      String comments) {
    report.setMonth(month);
    report.setTitle(title);
    report.setSalesInfo(salesInfo);
    report.setNextMonthOvertimeHours(nextMonthOvertimeHours);
    report.setNextMonthOvertimeReason(nextMonthOvertimeReason);
    report.setThisMonthOvertimeHours(thisMonthOvertimeHours);
    report.setThisMonthOvertimeReason(thisMonthOvertimeReason);
    report.setConditions(new HashMap<>(conditions));
    report.setComments(comments);
  }

  private void validateConditions(Map<String, String> conditions) {
    if (!conditions.keySet().containsAll(REQUIRED_CONDITION_KEYS)) {
      throw new BusinessException(ErrorCodes.VAL_001, "conditions に必須キーが不足しています");
    }
    conditions.forEach((k, v) -> {
      if (!REQUIRED_CONDITION_KEYS.contains(k) || !CONDITION_VALUES.contains(v)) {
        throw new BusinessException(ErrorCodes.VAL_001, "conditions の値が不正です");
      }
    });
  }
}
