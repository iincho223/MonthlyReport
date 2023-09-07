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
public class TopSyainInfoEntity {

    /** 部署コード */
    @Id
    @Column(name="BUSYO_CD")
    private String busyoCd;

    /** チームコード */
    @Id
    @Column(name="TM_CD")
    private String tmCd;

    /** 権限コード */
    @Column(name="KENGEN_CD")
    private String kengenCd;

}
