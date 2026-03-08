package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.dto.request.EmptyRequest;
import co.jp.monthlyreport.api.dto.request.LoginRequest;
import co.jp.monthlyreport.api.dto.request.RefreshRequest;
import co.jp.monthlyreport.api.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ApiResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return ApiResponse.ok(authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ApiResponse logout(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody(required = false) EmptyRequest request) {
    Map<String, Object> params = authService.logout(authorization);
    return ApiResponse.ok(params);
  }
}
