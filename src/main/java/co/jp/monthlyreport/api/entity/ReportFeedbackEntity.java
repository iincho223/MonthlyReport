package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 月報回答（DB設計.md 5.8 report_feedbacks）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_feedbacks")
public class ReportFeedbackEntity extends BaseEntity {

  /** 月報ID（reports.report_id と 1:1） */
  @Id
  @Column(name = "report_id", columnDefinition = "char(26)")
  private String reportId;

  /** フィードバック本文 */
  @Column(name = "feedback_comment", columnDefinition = "text")
  private String feedbackComment;

  /** 回答者ユーザーID */
  @Column(name = "responder_user_id", nullable = false)
  private Long responderUserId;

  /** 回答日時 */
  @Column(name = "responded_at", nullable = false)
  private OffsetDateTime respondedAt;
}
