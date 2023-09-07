package co.jp.monthlyreport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * ログイン認証リクエストクラス.
 */
@Data
public class LoginAuthRequest {
    /**
     * 社員番号
     */
    @NotBlank
    @Pattern(regexp="^[a-z,A-Z,0-9]+")
    private String syainNo;

    /**
     * 会社コード
     */
    @NotBlank
    @Pattern(regexp="^[a-z,A-Z,0-9]+")
    private String kaisyaCd;

    /**
     * パスワード
     */
    @NotBlank
    private String password;
}
