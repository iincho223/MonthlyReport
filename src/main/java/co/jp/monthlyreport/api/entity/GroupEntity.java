package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** グループマスタ（DB設計.md 5.3 groups）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class GroupEntity extends BaseEntity {

  /** グループコード */
  @Id
  @Column(name = "group_code", length = 20)
  private String groupCode;

  /** グループ名 */
  @Column(name = "group_name", nullable = false, length = 100)
  private String groupName;

  /** 所属営業所コード */
  @Column(name = "office_code", nullable = false, length = 20)
  private String officeCode;

  /** グループリーダー（GL）のユーザーID */
  @Column(name = "gl_user_id", nullable = false)
  private Long glUserId;
}
