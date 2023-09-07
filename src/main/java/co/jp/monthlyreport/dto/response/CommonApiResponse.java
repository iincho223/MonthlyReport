package co.jp.monthlyreport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 共通APIレスポンス項目.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonApiResponse {
    /**
     * 返却ステータス.
     */
    protected String resultStatus;
    /**
     * 返却メッセージ.
     */
    protected String resultMsg;
    /**
     * 返却コード.
     */
    protected String resultCd;
}
