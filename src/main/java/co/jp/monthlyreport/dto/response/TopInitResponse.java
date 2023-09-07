package co.jp.monthlyreport.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class TopInitResponse extends CommonApiResponse {
    /** メッセージ */
    private List<String> msgList;

    /** メンバ設定表示フラグ */
    private boolean menberSettingViewFlg;
}
