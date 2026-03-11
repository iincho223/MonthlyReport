package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.DashboardSummaryRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
/**
 * ダッシュボード API コントローラ。
 * インプット: 認可ヘッダと集計条件。
 * アウトプット: ダッシュボード集計結果。
 */
public class DashboardController {
  private final AuthService authService;
  private final DashboardService dashboardService;

  public DashboardController(AuthService authService, DashboardService dashboardService) {
    this.authService = authService;
    this.dashboardService = dashboardService;
  }

  /**
   * ダッシュボード集計 API。
   * インプット: authorization 認可ヘッダ、request 集計対象月。
   * アウトプット: 総件数と未回答件数。
   *
   * @param authorization 認可ヘッダ
   * @param request 集計リクエスト
   * @return 集計結果
   */
  @PostMapping("/summary")
  public ApiResponse summary(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody DashboardSummaryRequest request) {
    // 認証ユーザーを解決して集計スコープを確定する。
    AuthUser user = authService.resolveAuthUser(authorization);
    // ダッシュボード用集計を実行する。
    return ApiResponse.ok(dashboardService.summary(user, request.month()));
  }
}
