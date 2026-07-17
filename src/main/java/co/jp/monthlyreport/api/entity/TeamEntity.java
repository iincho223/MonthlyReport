package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** チームマスタ（DB設計.md 5.4 teams）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "teams")
public class TeamEntity extends BaseEntity {

  /** チームコード */
  @Id
  @Column(name = "team_code", length = 20)
  private String teamCode;

  /** チーム名 */
  @Column(name = "team_name", nullable = false, length = 100)
  private String teamName;

  /** 所属グループコード */
  @Column(name = "group_code", nullable = false, length = 20)
  private String groupCode;

  /** 所属営業所コード */
  @Column(name = "office_code", nullable = false, length = 20)
  private String officeCode;

  /** チームリーダー（TL）のユーザーID */
  @Column(name = "tl_user_id", nullable = false)
  private Long tlUserId;
}
