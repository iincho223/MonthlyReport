package co.jp.monthlyreport.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ユーザーアカウント。データの箱として各フィールドを個別に get/set できる。
 * Lombok により getter・setter・equals・hashCode・両コンストラクタを自動生成する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // パスワードをログ等に出力しないよう除外（@Data の @ToString を上書き）
public class UserAccount {

  /** ユーザーID */
  private Long userId;
  /** 社員番号 */
  private String employeeNo;
  /** 氏名 */
  private String name;
  /** パスワード（ハッシュ）*/
  private String password;
  /** ロール */
  private UserRole role;
  /** 拠点コード */
  private String officeCode;
  /** チームコード */
  private String teamCode;
  /** 有効フラグ */
  private boolean active;
  /** 論理削除フラグ */
  private boolean deleted;
}
