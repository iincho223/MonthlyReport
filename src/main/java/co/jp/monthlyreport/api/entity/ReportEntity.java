package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 月報ヘッダ（DB設計.md 5.6 reports）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reports")
public class ReportEntity extends BaseEntity {

  /** 月報ID（ULID） */
  @Id
  @Column(name = "report_id", length = 26)
  private String reportId;

  /** 対象月（yyyy-MM） */
  @Column(name = "report_month", nullable = false, length = 7)
  private String reportMonth;

  /** 営業情報 */
  @Column(name = "sales_info", columnDefinition = "text")
  private String salesInfo;

  /** 来月残業見込み時間 */
  @Column(name = "next_month_overtime_hours")
  private Integer nextMonthOvertimeHours;

  /** 来月残業見込み理由 */
  @Column(name = "next_month_overtime_reason", length = 255)
  private String nextMonthOvertimeReason;

  /** 今月残業実績時間 */
  @Column(name = "this_month_overtime_hours")
  private Integer thisMonthOvertimeHours;

  /** 今月残業実績理由 */
  @Column(name = "this_month_overtime_reason", length = 255)
  private String thisMonthOvertimeReason;

  /** コメント */
  @Column(name = "comments", columnDefinition = "text")
  private String comments;

  /** ステータス（DRAFT/SUBMITTED/PENDING_FEEDBACK/FEEDBACKED） */
  @Column(name = "status", nullable = false, length = 30)
  private String status;

  /** 作成者ユーザーID */
  @Column(name = "author_user_id", nullable = false)
  private Long authorUserId;

  /** 作成時点営業所コード */
  @Column(name = "office_code", nullable = false, length = 20)
  private String officeCode;

  /** 作成時点チームコード */
  @Column(name = "team_code", nullable = false, length = 20)
  private String teamCode;
}
