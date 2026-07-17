package co.jp.monthlyreport.api.common;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * API 全体の例外を共通フォーマットへ変換するハンドラ。
 * インプット: 各 API 実行中に送出された例外。
 * アウトプット: クライアント仕様に沿った ApiResponse を保持する ResponseEntity。
 */
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * 業務例外を resultCd 付きの正常レスポンス形式で返す。
   * インプット: ex 業務例外。
   * アウトプット: 業務エラー形式のレスポンス。
   *
   * @param ex 業務例外
   * @return 業務エラーレスポンス
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse> handleBusiness(BusinessException ex) {
    // クライアント仕様に合わせ、業務例外は 200 応答の業務エラー形式で返却する。
    return ResponseEntity.ok(ApiResponse.businessError(ex.getResultCd(), ex.getMessage()));
  }

  /**
   * バリデーション関連例外から項目別メッセージを生成して返す。
   * インプット: ex バリデーション関連例外。
   * アウトプット: 入力不正コード付きの業務エラーレスポンス。
   *
   * @param ex バリデーション関連例外
   * @return 入力不正エラーレスポンス
   */
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiResponse> handleValidation(Exception ex) {
    String message;
    // Bean Validation 由来のエラー一覧を「項目:メッセージ」で連結する。
    if (ex instanceof MethodArgumentNotValidException manv) {
      message = manv.getBindingResult().getFieldErrors().stream()
          .map(err -> err.getField() + ":" + err.getDefaultMessage())
          .collect(Collectors.joining(", "));
    // バインド失敗時も同じ形式で項目エラーを返す。
    } else if (ex instanceof BindException be) {
      message = be.getBindingResult().getFieldErrors().stream()
          .map(err -> err.getField() + ":" + err.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else {
      // それ以外の入力例外はメッセージをそのまま利用する。
      message = ex.getMessage();
    }
    // 入力不正コードで統一して返却する。
    return ResponseEntity.ok(ApiResponse.businessError(ErrorCodes.VAL_001, message));
  }

  /**
   * 想定外例外をシステムエラーとして返す。
   * インプット: ex 想定外例外。
   * アウトプット: 固定文言のシステムエラーレスポンス。
   *
   * @param ex 想定外例外
   * @return システムエラーレスポンス
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleSystem(Exception ex) {
    // 想定外例外はレスポンスへ出さないため、原因追跡できるようサーバーログへ記録する。
    log.error("想定外のシステムエラーが発生しました。", ex);
    // 内部詳細は隠蔽し、利用者向けの固定メッセージを返す。
    return ResponseEntity.ok(ApiResponse.systemError("システムエラーが発生しました"));
  }
}
