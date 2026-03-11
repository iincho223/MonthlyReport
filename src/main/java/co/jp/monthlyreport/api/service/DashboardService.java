package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.model.ReportRecord;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
/**
 * ダッシュボード用集計サービス。
 * インプット: 認証ユーザーと対象月。
 * アウトプット: 月報件数と未回答件数の集計値。
 */
public class DashboardService {
  private final ReportService reportService;

  public DashboardService(ReportService reportService) {
    this.reportService = reportService;
  }

  /**
   * ダッシュボード集計を返す。
   * インプット: user 認証ユーザー、month 対象月。
   * アウトプット: 集計結果マップ。
   *
   * @param user 認証ユーザー
   * @param month 対象月
   * @return 集計結果
   */
  public Map<String, Object> summary(AuthUser user, String month) {
    // ユーザーの参照スコープに合わせて月報を絞り込む。
    List<ReportRecord> scoped = reportService.scopedReports(user).stream()
        .filter(r -> month == null || month.isBlank() || month.equals(r.getMonth()))
        .toList();

    // 未回答の月報件数をカウントする。
    long pending = scoped.stream().filter(r -> !r.hasFeedback()).count();

    return Map.of(
        ResponseKeys.TOTAL_REPORTS, scoped.size(),
        ResponseKeys.PENDING_FEEDBACK_COUNT, pending);
  }
}
