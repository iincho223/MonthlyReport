package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** エスカレーション（DB設計.md 5.9 escalations）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "escalations")
public class EscalationEntity extends BaseEntity {

  /** エスカレーションID */
  @Id
  @Column(name = "escalation_id", length = 20)
  private String escalationId;

  /** 案件タイトル */
  @Column(name = "title", nullable = false, length = 200)
  private String title;

  /** 対象メンバー氏名 */
  @Column(name = "target_employee_name", nullable = false, length = 100)
  private String targetEmployeeName;

  /** 対象チーム名 */
  @Column(name = "target_team", nullable = false, length = 100)
  private String targetTeam;

  /** 詳細内容 */
  @Column(name = "description", columnDefinition = "text")
  private String description;

  /** 重要度（LOW/MEDIUM/HIGH） */
  @Column(name = "severity", nullable = false, length = 10)
  private String severity;

  /** ステータス（PENDING/ONGOING/RESOLVED） */
  @Column(name = "status", nullable = false, length = 10)
  private String status;

  /** 対応期日 */
  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  /** 完了期日 */
  @Column(name = "resolved_date")
  private LocalDate resolvedDate;

  /** 起票者ユーザーID */
  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  /** 起票者氏名（スナップショット） */
  @Column(name = "created_by_name", nullable = false, length = 100)
  private String createdByName;

  /** 起票者ロール（スナップショット） */
  @Column(name = "created_by_role", nullable = false, length = 20)
  private String createdByRole;

  /** 対応担当者ユーザーID（一度設定した場合、未設定への変更は不可） */
  @Column(name = "assignee_user_id")
  private Long assigneeUserId;

  /** 起票時点営業所コード（スコープ判定用） */
  @Column(name = "office_code", nullable = false, length = 20)
  private String officeCode;

  /** 起票時点チームコード（スコープ判定用） */
  @Column(name = "team_code", nullable = false, length = 20)
  private String teamCode;
}
