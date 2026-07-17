package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 営業所マスタ（DB設計.md 5.2 offices）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "offices")
public class OfficeEntity extends BaseEntity {

  /** 営業所コード */
  @Id
  @Column(name = "office_code", length = 20)
  private String officeCode;

  /** 営業所名 */
  @Column(name = "office_name", nullable = false, length = 100)
  private String officeName;
}
