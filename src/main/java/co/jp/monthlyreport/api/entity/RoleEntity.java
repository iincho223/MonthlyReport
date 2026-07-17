package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ロールマスタ（DB設計.md 5.1 roles）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {

  /** ロールコード（NG/TM/TL/GL/OM/SP/SM/SA） */
  @Id
  @Column(name = "role_code", length = 20)
  private String roleCode;

  /** 表示名 */
  @Column(name = "role_name", nullable = false, length = 100)
  private String roleName;

  /** 権限序列 */
  @Column(name = "role_rank", nullable = false, columnDefinition = "tinyint")
  private int roleRank;
}
