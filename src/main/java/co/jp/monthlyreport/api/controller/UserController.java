package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.EmptyRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
/**
 * ユーザー API コントローラ。
 * インプット: 認証済みユーザーに関するリクエスト。
 * アウトプット: 自分情報を格納した ApiResponse。
 */
public class UserController {
  private final AuthService authService;
  private final UserService userService;

  public UserController(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  /**
   * 自分情報取得 API。
   * インプット: authorization 認可ヘッダ。
   * アウトプット: ログインユーザーのプロフィール。
   *
   * @param authorization 認可ヘッダ
   * @param request 空リクエスト
   * @return 自分情報
   */
  @PostMapping("/me")
  public ApiResponse me(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody(required = false) EmptyRequest request) {
    // 認可ヘッダから認証ユーザーを解決する。
    AuthUser user = authService.resolveAuthUser(authorization);
    // ユーザーサービスで返却項目を整形して返す。
    return ApiResponse.ok(userService.me(user));
  }
}
