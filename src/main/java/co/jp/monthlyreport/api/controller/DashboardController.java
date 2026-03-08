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
public class DashboardController {
  private final AuthService authService;
  private final DashboardService dashboardService;

  public DashboardController(AuthService authService, DashboardService dashboardService) {
    this.authService = authService;
    this.dashboardService = dashboardService;
  }

  @PostMapping("/summary")
  public ApiResponse summary(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody DashboardSummaryRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(dashboardService.summary(user, request.month()));
  }
}
