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
/**
 * 月報 API コントローラ。
 * インプット: 月報検索/詳細/作成/更新/削除/回答更新のリクエスト。
 * アウトプット: 月報処理結果を格納した ApiResponse。
 */
public class ReportController {
  private final AuthService authService;
  private final ReportService reportService;

  public ReportController(AuthService authService, ReportService reportService) {
    this.authService = authService;
    this.reportService = reportService;
  }

  /**
   * 月報一覧検索 API。
   * インプット: authorization 認可ヘッダ、request 検索条件。
   * アウトプット: 月報一覧とページング情報。
   *
   * @param authorization 認可ヘッダ
   * @param request 検索リクエスト
   * @return 検索結果
   */
  @PostMapping("/search")
  public ApiResponse search(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportSearchRequest request) {
    // 認可ヘッダから認証ユーザー情報を復元する。
    AuthUser user = authService.resolveAuthUser(authorization);
    // サービスでスコープ検索を実行する。
    return ApiResponse.ok(reportService.search(user, request));
  }

  /**
   * 月報詳細取得 API。
   * インプット: authorization 認可ヘッダ、request 月報ID。
   * アウトプット: 月報詳細。
   *
   * @param authorization 認可ヘッダ
   * @param request 詳細取得リクエスト
   * @return 詳細結果
   */
  @PostMapping("/detail")
  public ApiResponse detail(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportDetailRequest request) {
    // 認証ユーザーを解決し、参照権限判定をサービス側で実施する。
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.detail(user, request.reportId()));
  }

  /**
   * 月報作成 API。
   * インプット: authorization 認可ヘッダ、request 作成内容。
   * アウトプット: 作成した月報ID。
   *
   * @param authorization 認可ヘッダ
   * @param request 作成リクエスト
   * @return 作成結果
   */
  @PostMapping("/create")
  public ApiResponse create(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportCreateRequest request) {
    // 認証ユーザーで作成者情報を確定する。
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.create(user, request));
  }

  /**
   * 月報更新 API。
   * インプット: authorization 認可ヘッダ、request 更新内容。
   * アウトプット: 更新結果。
   *
   * @param authorization 認可ヘッダ
   * @param request 更新リクエスト
   * @return 更新結果
   */
  @PostMapping("/update")
  public ApiResponse update(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportUpdateRequest request) {
    // 認証ユーザーを解決して更新権限をサービスで確認する。
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.update(user, request));
  }

  /**
   * 月報削除 API。
   * インプット: authorization 認可ヘッダ、request 月報ID。
   * アウトプット: 削除結果。
   *
   * @param authorization 認可ヘッダ
   * @param request 削除リクエスト
   * @return 削除結果
   */
  @PostMapping("/delete")
  public ApiResponse delete(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody ReportDeleteRequest request) {
    // 認証済みユーザーとして削除処理を実行する。
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.delete(user, request.reportId()));
  }

  /**
   * 月報回答更新 API。
   * インプット: authorization 認可ヘッダ、request 回答内容。
   * アウトプット: 回答更新結果。
   *
   * @param authorization 認可ヘッダ
   * @param request 回答更新リクエスト
   * @return 回答更新結果
   */
  @PostMapping("/feedback/update")
  public ApiResponse updateFeedback(
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody FeedbackUpdateRequest request) {
    // 認証ユーザー情報を取得して回答更新を実行する。
    AuthUser user = authService.resolveAuthUser(authorization);
    return ApiResponse.ok(reportService.updateFeedback(user, request));
  }
}
