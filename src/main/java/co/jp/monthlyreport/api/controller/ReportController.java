package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.FeedbackUpdateRequest;
import co.jp.monthlyreport.api.dto.request.ReportCreateRequest;
import co.jp.monthlyreport.api.dto.request.ReportDeleteRequest;
import co.jp.monthlyreport.api.dto.request.ReportDetailRequest;
import co.jp.monthlyreport.api.dto.request.ReportSearchRequest;
import co.jp.monthlyreport.api.dto.request.ReportUpdateRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
  private final AuthService authService;
  private final ReportService reportService;

  public ReportController(AuthService authService, ReportService reportService) {
    this.authService = authService;
    this.reportService = reportService;
  }

  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportSearchRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.search(user, request));
  }

  @PostMapping("/detail")
  public ApiResponse detail(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportDetailRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.detail(user, request.reportId()));
  }

  @PostMapping("/create")
  public ApiResponse create(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportCreateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.create(user, request));
  }

  @PostMapping("/update")
  public ApiResponse update(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportUpdateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.update(user, request));
  }

  @PostMapping("/delete")
  public ApiResponse delete(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportDeleteRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.delete(user, request.reportId()));
  }

  @PostMapping("/feedback/update")
  public ApiResponse updateFeedback(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody FeedbackUpdateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.updateFeedback(user, request));
  }
}
