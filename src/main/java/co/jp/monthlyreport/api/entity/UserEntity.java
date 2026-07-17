package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ユーザーマスタ（DB設計.md 5.5 users）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

  /** 内部ID（auto_increment） */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  /** 社員番号 */
  @Column(name = "employee_no", nullable = false, length = 20)
  private String employeeNo;

  /** 氏名 */
  @Column(name = "user_name", nullable = false, length = 100)
  private String userName;

  /** パスワードハッシュ（BCrypt） */
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  /** ロールコード */
  @Column(name = "role_code", nullable = false, length = 20)
  private String roleCode;

  /** 拠点コード */
  @Column(name = "office_code", nullable = false, length = 20)
  private String officeCode;

  /** チームコード */
  @Column(name = "team_code", nullable = false, length = 20)
  private String teamCode;

  /** 有効フラグ */
  @Column(name = "is_active", nullable = false)
  private boolean active;
}
