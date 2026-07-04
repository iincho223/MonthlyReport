package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.EmptyRequest;
import co.jp.monthlyreport.api.dto.request.UserCreateRequest;
import co.jp.monthlyreport.api.dto.request.UserDeleteRequest;
import co.jp.monthlyreport.api.dto.request.UserSearchRequest;
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
 * アウトプット: 自分情報・ユーザー管理処理結果を格納した ApiResponse。
 */
public class UserController {
  private final AuthService authService;
  private final UserService userService;

  public UserController(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  /**
   * 自分情報取得 API (API-04)。
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

  /**
   * ユーザー一覧検索 API (API-17)。
   * インプット: authorization 認可ヘッダ、request 検索条件。
   * アウトプット: ユーザー一覧とページング情報。
   *
   * @param authorization 認可ヘッダ
   * @param request       検索リクエスト
   * @return 検索結果
   */
  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody UserSearchRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(userService.search(user, request));
  }

  /**
   * ユーザー登録 API (API-18)。
   * インプット: authorization 認可ヘッダ、request 登録内容。
   * アウトプット: 登録したユーザーID・社員番号。
   *
   * @param authorization 認可ヘッダ
   * @param request       登録リクエスト
   * @return 登録結果
   */
  @PostMapping("/create")
  public ApiResponse create(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody UserCreateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(userService.create(user, request));
  }

  /**
   * ユーザー削除 API (API-19)。
   * インプット: authorization 認可ヘッダ、request 削除対象ユーザーID。
   * アウトプット: 削除結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       削除リクエスト
   * @return 削除結果
   */
  @PostMapping("/delete")
  public ApiResponse delete(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody UserDeleteRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(userService.delete(user, request.userId()));
  }
}
