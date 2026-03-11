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
/**
 * 認証系 API コントローラ。
 * インプット: ログイン/リフレッシュ/ログアウトのリクエスト。
 * アウトプット: 認証結果を格納した ApiResponse。
 */
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * ログイン API。
   * インプット: request 社員番号とパスワード。
   * アウトプット: アクセストークンとリフレッシュトークン。
   *
   * @param request ログインリクエスト
   * @return ログイン結果
   */
  @PostMapping("/login")
  public ApiResponse login(@Valid @RequestBody LoginRequest request) {
    // 認証サービスへ処理委譲し、共通レスポンスで返却する。
    return ApiResponse.ok(authService.login(request));
  }

  /**
   * トークン再発行 API。
   * インプット: request リフレッシュトークン。
   * アウトプット: 新しいアクセストークンとリフレッシュトークン。
   *
   * @param request リフレッシュリクエスト
   * @return 再発行結果
   */
  @PostMapping("/refresh")
  public ApiResponse refresh(@Valid @RequestBody RefreshRequest request) {
    // リフレッシュトークンを検証して新しいトークンを発行する。
    return ApiResponse.ok(authService.refresh(request.refreshToken()));
  }

  /**
   * ログアウト API。
   * インプット: authorization 認可ヘッダ。
   * アウトプット: ログアウト成功結果。
   *
   * @param authorization 認可ヘッダ
   * @param request 空リクエスト
   * @return ログアウト結果
   */
  @PostMapping("/logout")
  public ApiResponse logout(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody(required = false) EmptyRequest request) {
    // 認証ユーザーのトークンセッションを無効化する。
    Map<String, Object> params = authService.logout(authorization);
    return ApiResponse.ok(params);
  }
}
