package co.jp.monthlyreport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ログイン認証レスポンスクラス.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class LoginAuthResponse extends CommonApiResponse {
    /**
     * 認証結果.
     */
    private String success;
}
