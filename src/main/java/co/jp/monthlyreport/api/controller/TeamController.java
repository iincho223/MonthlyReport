package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.TeamCreateRequest;
import co.jp.monthlyreport.api.dto.request.TeamDeleteRequest;
import co.jp.monthlyreport.api.dto.request.TeamSearchRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
/**
 * チーム API コントローラ。
 * インプット: チーム検索/登録/削除のリクエスト。
 * アウトプット: チーム処理結果を格納した ApiResponse。
 */
public class TeamController {

  private final AuthService authService;
  private final TeamService teamService;

  public TeamController(AuthService authService, TeamService teamService) {
    this.authService = authService;
    this.teamService = teamService;
  }

  /**
   * チーム一覧検索 API (API-23)。
   * インプット: authorization 認可ヘッダ、request 検索条件。
   * アウトプット: チーム一覧。
   *
   * @param authorization 認可ヘッダ
   * @param request       検索リクエスト
   * @return 検索結果
   */
  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody TeamSearchRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(teamService.search(user, request));
  }

  /**
   * チーム登録 API (API-24)。
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
      @Valid @RequestBody TeamCreateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(teamService.create(user, request));
  }

  /**
   * チーム削除 API (API-25)。
   * インプット: authorization 認可ヘッダ、request 削除対象チームコード。
   * アウトプット: 削除結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       削除リクエスト
   * @return 削除結果
   */
  @PostMapping("/delete")
  public ApiResponse delete(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody TeamDeleteRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(teamService.delete(user, request.teamCode()));
  }
}
