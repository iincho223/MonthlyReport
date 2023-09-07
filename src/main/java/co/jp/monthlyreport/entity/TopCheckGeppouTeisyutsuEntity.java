package co.jp.monthlyreport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TopCheckGeppouTeisyutsuEntity {

    /** 月報提出数 */
    @Id
    @Column(name="TEISYUTSU_NUM")
    private Integer teisyutsuNum;

}
