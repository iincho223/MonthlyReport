package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** エスカレーション対応ログ（DB設計.md 5.10 escalation_logs、追記専用）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "escalation_logs")
public class EscalationLogEntity extends BaseEntity {

  /** ログID（ULID） */
  @Id
  @Column(name = "log_id", columnDefinition = "char(26)")
  private String logId;

  /** 対象エスカレーションID */
  @Column(name = "escalation_id", nullable = false, length = 20)
  private String escalationId;

  /** 対応ログ本文 */
  @Column(name = "log_text", nullable = false, columnDefinition = "text")
  private String logText;

  /** 記録者ユーザーID */
  @Column(name = "author_user_id", nullable = false)
  private Long authorUserId;

  /** 記録者氏名（スナップショット） */
  @Column(name = "author_name", nullable = false, length = 100)
  private String authorName;

  /** 記録日時 */
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
