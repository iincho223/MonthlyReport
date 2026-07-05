package co.jp.monthlyreport.api.common;

/**
 * バリデーション用定数クラス。
 * {@code @Pattern(regexp = ...)} / {@code @Size} / {@code @Min} / {@code @Max} に指定する値は
 * コンパイル時定数である必要があるため、{@code static final} で一元管理する。
 */
public final class ValidationConstants {

  // ---------------------------------------------------------------------------
  // 正規表現
  // ---------------------------------------------------------------------------

  /** 社員番号：半角英数字のみ */
  public static final String REGEX_EMPLOYEE_NO = "^[A-Za-z0-9]+$";

  /** 年月（必須）：yyyy-MM 形式 */
  public static final String REGEX_YEAR_MONTH = "^\\d{4}-\\d{2}$";

  /** 年月（任意）：空文字または yyyy-MM 形式 */
  public static final String REGEX_YEAR_MONTH_OPTIONAL = "^$|^\\d{4}-\\d{2}$";

  /** 日付：yyyy-MM-dd 形式 */
  public static final String REGEX_DATE = "^\\d{4}-\\d{2}-\\d{2}$";

  /** グループ/チームコード：英数字とアンダースコアのみ */
  public static final String REGEX_CODE = "^[A-Za-z0-9_]+$";

  // ---------------------------------------------------------------------------
  // 文字列長 (@Size)
  // ---------------------------------------------------------------------------

  /** 社員番号の最大文字数 */
  public static final int MAX_EMPLOYEE_NO = 20;

  /** パスワードの最大文字数（ハッシュ長を想定） */
  public static final int MAX_PASSWORD = 128;

  /** フィードバックコメントの最大文字数 */
  public static final int MAX_FEEDBACK_COMMENT = 2000;

  // ---------------------------------------------------------------------------
  // 数値範囲 (@Min / @Max)
  // ---------------------------------------------------------------------------

  /** ページ番号の最小値（1始まり） */
  public static final int MIN_PAGE = 1;

  /** 1ページあたりの最小件数 */
  public static final int MIN_PAGE_SIZE = 1;

  /** 1ページあたりの最大件数 */
  public static final int MAX_PAGE_SIZE = 100;

  /** 残業時間の最小値（時間） */
  public static final int MIN_OVERTIME_HOURS = 0;

  /** 残業時間の最大値（時間） */
  public static final int MAX_OVERTIME_HOURS = 300;

  /** エスカレーション タイトルの最大文字数 */
  public static final int MAX_ESC_TITLE = 200;

  /** エスカレーション 詳細内容の最大文字数 */
  public static final int MAX_ESC_DESCRIPTION = 2000;

  /** 対応ログの最大文字数 */
  public static final int MAX_ESC_LOG_TEXT = 2000;

  /** 氏名の最大文字数 */
  public static final int MAX_NAME = 100;

  /** グループ名/チーム名の最大文字数 */
  public static final int MAX_GROUP_TEAM_NAME = 100;

  /** グループコード/チームコードの最大文字数 */
  public static final int MAX_CODE_LENGTH = 32;

  // ---------------------------------------------------------------------------
  // 提出・フィードバック期限
  // ---------------------------------------------------------------------------

  /** フィードバック・提出締切日（毎月 N 日） */
  public static final int DEADLINE_DAY = 5;

  // インスタンス化禁止
  private ValidationConstants() {}
}
