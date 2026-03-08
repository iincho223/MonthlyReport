package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.model.ReportRecord;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
  private final ReportService reportService;

  public DashboardService(ReportService reportService) {
    this.reportService = reportService;
  }

  public Map<String, Object> summary(AuthUser user, String month) {
    List<ReportRecord> scoped = reportService.scopedReports(user).stream()
        .filter(r -> month == null || month.isBlank() || month.equals(r.getMonth()))
        .toList();

    long pending = scoped.stream().filter(r -> !r.hasFeedback()).count();

    return Map.of(
        "totalReports", scoped.size(),
        "pendingFeedbackCount", pending);
  }
}
