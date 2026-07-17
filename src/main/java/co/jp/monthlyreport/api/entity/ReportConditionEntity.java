package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 月報コンディション（DB設計.md 5.7 report_conditions）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_conditions")
public class ReportConditionEntity extends BaseEntity {

  /** 月報ID（reports.report_id と 1:1） */
  @Id
  @Column(name = "report_id", length = 26)
  private String reportId;

  @Column(name = "physical", nullable = false, length = 10)
  private String physical;

  @Column(name = "stress", nullable = false, length = 10)
  private String stress;

  @Column(name = "relationships", nullable = false, length = 10)
  private String relationships;

  @Column(name = "worries", nullable = false, length = 10)
  private String worries;

  @Column(name = "fatigue", nullable = false, length = 10)
  private String fatigue;

  @Column(name = "sleep", nullable = false, length = 10)
  private String sleep;

  @Column(name = "motivation", nullable = false, length = 10)
  private String motivation;
}
