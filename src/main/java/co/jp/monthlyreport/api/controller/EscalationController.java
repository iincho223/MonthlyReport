package co.jp.monthlyreport.api.controller;

import co.jp.monthlyreport.api.common.ApiResponse;
import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.dto.request.EscalationCreateRequest;
import co.jp.monthlyreport.api.dto.request.EscalationDetailRequest;
import co.jp.monthlyreport.api.dto.request.EscalationLogAddRequest;
import co.jp.monthlyreport.api.dto.request.EscalationSearchRequest;
import co.jp.monthlyreport.api.dto.request.EscalationUpdateRequest;
import co.jp.monthlyreport.api.service.AuthService;
import co.jp.monthlyreport.api.service.EscalationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/escalations")
/**
 * エスカレーション API コントローラ。
 * インプット: エスカレーション検索/詳細/作成/更新/ログ追加のリクエスト。
 * アウトプット: エスカレーション処理結果を格納した ApiResponse。
 */
public class EscalationController {

  private final AuthService authService;
  private final EscalationService escalationService;

  public EscalationController(AuthService authService, EscalationService escalationService) {
    this.authService = authService;
    this.escalationService = escalationService;
  }

  /**
   * エスカレーション一覧検索 API (API-12)。
   * インプット: authorization 認可ヘッダ、request 検索条件。
   * アウトプット: エスカレーション一覧とページング情報。
   *
   * @param authorization 認可ヘッダ
   * @param request       検索リクエスト
   * @return 検索結果
   */
  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody EscalationSearchRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(escalationService.search(user, request));
  }

  /**
   * エスカレーション詳細取得 API (API-13)。
   * インプット: authorization 認可ヘッダ、request エスカレーションID。
   * アウトプット: エスカレーション詳細（履歴含む）。
   *
   * @param authorization 認可ヘッダ
   * @param request       詳細取得リクエスト
   * @return 詳細結果
   */
  @PostMapping("/detail")
  public ApiResponse detail(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody EscalationDetailRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(escalationService.detail(user, request.escalationId()));
  }

  /**
   * エスカレーション作成 API (API-14)。
   * インプット: authorization 認可ヘッダ、request 起票内容。
   * アウトプット: 作成したエスカレーションID。
   *
   * @param authorization 認可ヘッダ
   * @param request       作成リクエスト
   * @return 作成結果
   */
  @PostMapping("/create")
  public ApiResponse create(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody EscalationCreateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(escalationService.create(user, request));
  }

  /**
   * エスカレーション更新 API (API-15)。
   * インプット: authorization 認可ヘッダ、request 更新内容。
   * アウトプット: 更新結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       更新リクエスト
   * @return 更新結果
   */
  @PostMapping("/update")
  public ApiResponse update(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody EscalationUpdateRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(escalationService.update(user, request));
  }

  /**
   * 対応ログ追加 API (API-16)。
   * インプット: authorization 認可ヘッダ、request ログ内容。
   * アウトプット: ログ追加結果。
   *
   * @param authorization 認可ヘッダ
   * @param request       ログ追加リクエスト
   * @return ログ追加結果
   */
  @PostMapping("/log/add")
  public ApiResponse addLog(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody EscalationLogAddRequest request) {
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(escalationService.addLog(user, request));
  }
}
