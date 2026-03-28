package co.jp.monthlyreport.api.common;

/**
 * API 共通レスポンス。
 * インプット: 各 API 処理結果またはエラー情報。
 * アウトプット: resultStatus/resultMsg/resultCd/params を持つレスポンス本体。
 */
public record ApiResponse(String resultStatus, String resultMsg, String resultCd, Object params) {

  /**
   * 正常系レスポンスを生成する。
   * インプット: params 正常時の返却データ。
   * アウトプット: resultStatus=0 のレスポンス。
   *
   * @param params 正常時返却データ
   * @return 正常レスポンス
   */
  public static ApiResponse ok(Object params) {
    return new ApiResponse("0", null, null, params);
  }

  /**
   * 業務エラーレスポンスを生成する。
   * インプット: resultCd 業務エラーコード、resultMsg エラーメッセージ。
   * アウトプット: resultStatus=1 のレスポンス。
   *
   * @param resultCd 業務エラーコード
   * @param resultMsg エラーメッセージ
   * @return 業務エラーレスポンス
   */
  public static ApiResponse businessError(String resultCd, String resultMsg) {
    return new ApiResponse("1", resultMsg, resultCd, null);
  }

  /**
   * システムエラーレスポンスを生成する。
   * インプット: resultMsg 利用者向けエラーメッセージ。
   * アウトプット: resultStatus=9 のレスポンス。
   *
   * @param resultMsg 利用者向けエラーメッセージ
   * @return システムエラーレスポンス
   */
  public static ApiResponse systemError(String resultMsg) {
    return new ApiResponse("9", resultMsg, ErrorCodes.SYS_500, null);
  }
}
