package co.jp.monthlyreport.api.common;

/**
 * messages.properties に定義したメッセージキーの定数クラス。
 * サービス層で {@code MessageSource#getMessage} を呼ぶ際に使用する。
 * キーを変更する場合はここと messages.properties を併せて修正すること。
 */
public final class MessageKeys {

  // -------------------------------------------------------------------------
  // 認証エラー (AUTH)
  // -------------------------------------------------------------------------

  /** 認証失敗 */
  public static final String AUTH_LOGIN_FAILED = "error.auth.login_failed";
  /** リフレッシュトークン無効 */
  public static final String AUTH_REFRESH_TOKEN_INVALID = "error.auth.refresh_token_invalid";
  /** リフレッシュトークン失効 */
  public static final String AUTH_REFRESH_TOKEN_EXPIRED = "error.auth.refresh_token_expired";
  /** 認証情報が見つからない */
  public static final String AUTH_USER_NOT_FOUND = "error.auth.user_not_found";
  /** 未認証 */
  public static final String AUTH_UNAUTHORIZED = "error.auth.unauthorized";
  /** Authorization ヘッダ不正 */
  public static final String AUTH_INVALID_HEADER = "error.auth.invalid_header";
  /** ユーザー無効 */
  public static final String AUTH_USER_DISABLED = "error.auth.user_disabled";

  // -------------------------------------------------------------------------
  // 月報エラー (REPORT)
  // -------------------------------------------------------------------------

  /** 同一月の月報が既に存在する */
  public static final String REPORT_DUPLICATE_MONTH = "error.report.duplicate_month";
  /** 月報が見つからない */
  public static final String REPORT_NOT_FOUND = "error.report.not_found";
  /** 更新権限なし */
  public static final String REPORT_NO_UPDATE_PERMISSION = "error.report.no_update_permission";
  /** 削除権限なし */
  public static final String REPORT_NO_DELETE_PERMISSION = "error.report.no_delete_permission";
  /** 回答権限なし */
  public static final String REPORT_NO_FEEDBACK_PERMISSION = "error.report.no_feedback_permission";
  /** 自分の月報への回答禁止 */
  public static final String REPORT_FEEDBACK_SELF = "error.report.feedback_self";
  /** 参照権限なし */
  public static final String REPORT_NO_VIEW_PERMISSION = "error.report.no_view_permission";
  /** status 値不正 */
  public static final String REPORT_INVALID_STATUS = "error.report.invalid_status";
  /** conditions 必須キー不足 */
  public static final String REPORT_CONDITIONS_MISSING_KEY = "error.report.conditions_missing_key";
  /** conditions 値不正 */
  public static final String REPORT_CONDITIONS_INVALID_VALUE = "error.report.conditions_invalid_value";
  /** フィードバック期限切れ */
  public static final String REPORT_FEEDBACK_EXPIRED = "error.report.feedback_expired";

  // -------------------------------------------------------------------------
  // エスカレーションエラー (ESCALATION)
  // -------------------------------------------------------------------------

  /** エスカレーションが見つからない */
  public static final String ESC_NOT_FOUND = "error.escalation.not_found";
  /** エスカレーションへのアクセス権限なし */
  public static final String ESC_NO_ACCESS_PERMISSION = "error.escalation.no_access_permission";
  /** エスカレーション起票権限なし */
  public static final String ESC_NO_CREATE_PERMISSION = "error.escalation.no_create_permission";
  /** エスカレーション更新権限なし */
  public static final String ESC_NO_UPDATE_PERMISSION = "error.escalation.no_update_permission";
  /** severity 値不正 */
  public static final String ESC_INVALID_SEVERITY = "error.escalation.invalid_severity";
  /** status 値不正 */
  public static final String ESC_INVALID_STATUS = "error.escalation.invalid_status";

  // インスタンス化禁止
  private MessageKeys() {}
}
