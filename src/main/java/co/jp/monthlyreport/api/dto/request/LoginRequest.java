package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ログインリクエスト（API-01）。
 * インプット: 社員番号とパスワード。
 * アウトプット: バリデーション済みの認証情報。
 *
 * @param employeeNo 社員番号（半角英数字、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_EMPLOYEE_NO} 文字）
 * @param password   パスワード（最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_PASSWORD} 文字）
 */
public record LoginRequest(
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_EMPLOYEE_NO) @Size(max = ValidationConstants.MAX_EMPLOYEE_NO) String employeeNo,
    @NotBlank @Size(max = ValidationConstants.MAX_PASSWORD) String password) {}
