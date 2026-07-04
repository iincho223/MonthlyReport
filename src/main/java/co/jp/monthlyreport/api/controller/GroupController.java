package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.GroupCreateRequest;
import co.jp.monthlyreport.api.dto.request.GroupDeleteRequest;
import co.jp.monthlyreport.api.dto.request.GroupSearchRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
/**
 * グループ API コントローラ。
 * インプット: グループ検索/登録/削除のリクエスト。
 * アウトプット: グループ処理結果を格納した ApiResponse。
 */
public class GroupController {

  private final AuthService authService;
  private final GroupService groupService;

  public GroupController(AuthService authService, GroupService groupService) {
    this.authService = authService;
    this.groupService = groupService;
  }

  /**
   * グループ一覧検索 API (API-20)。
   * インプット: authorization 認可ヘッダ、request 検索条件。
   * アウトプット: グループ一覧。
   *
   * @param authorization 認可ヘッダ
   * @param request       検索リクエスト
   * @return 検索結果
   */
  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody GroupSearchRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(groupService.search(user, request));
  }

  /**
   * グループ登録 API (API-21)。
   * インプット: authorization 認可ヘッダ、request 登録内容。
   * アウトプット: 登録結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       登録リクエスト
   * @return 登録結果
   */
  @PostMapping("/create")
  public ApiResponse create(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody GroupCreateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(groupService.create(user, request));
  }

  /**
   * グループ削除 API (API-22)。
   * インプット: authorization 認可ヘッダ、request 削除対象グループコード。
   * アウトプット: 削除結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       削除リクエスト
   * @return 削除結果
   */
  @PostMapping("/delete")
  public ApiResponse delete(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody GroupDeleteRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(groupService.delete(user, request.groupCode()));
  }
}
