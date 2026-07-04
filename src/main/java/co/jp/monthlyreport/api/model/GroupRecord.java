package co.jp.monthlyreport.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * グループレコード。フィールドは Lombok により getter/setter を自動生成する。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class GroupRecord {

  /** グループコード */
  private String groupCode;
  /** グループ名 */
  private String groupName;
  /** 拠点コード */
  private String officeCode;
  /** グループリーダー(GL)のユーザーID */
  private Long glUserId;
  /** 論理削除フラグ */
  private boolean deleted;
}
