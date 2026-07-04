package co.jp.monthlyreport.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * チームレコード。フィールドは Lombok により getter/setter を自動生成する。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TeamRecord {

  /** チームコード */
  private String teamCode;
  /** チーム名 */
  private String teamName;
  /** 所属グループコード */
  private String groupCode;
  /** 拠点コード */
  private String officeCode;
  /** チームリーダー(TL)のユーザーID */
  private Long tlUserId;
  /** 論理削除フラグ */
  private boolean deleted;
}
