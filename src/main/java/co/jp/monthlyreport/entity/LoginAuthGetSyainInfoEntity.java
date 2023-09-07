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
public class LoginAuthGetSyainInfoEntity {
    /** パスワード */
    @Id
    @Column(name="PASS_WD")
    private String passWd;
}
