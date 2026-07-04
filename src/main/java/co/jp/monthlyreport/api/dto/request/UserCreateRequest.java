package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録リクエスト（API-18）。
 * インプット: 新規ユーザーの入力内容。
 * アウトプット: バリデーション済みのユーザー登録データ。
 *
 * @param employeeNo 社員番号（必須、半角英数字、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_EMPLOYEE_NO} 文字、一意）
 * @param name       氏名（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_NAME} 文字）
 * @param role       ロール（必須、ロール列挙値のみ）
 * @param officeCode 拠点コード（必須）
 * @param teamCode   チームコード（エンジニア（NG/TM）の場合は必須）
 * @param password   初期パスワード（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_PASSWORD} 文字）
 */
public record UserCreateRequest(
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_EMPLOYEE_NO) @Size(max = ValidationConstants.MAX_EMPLOYEE_NO) String employeeNo,
    @NotBlank @Size(max = ValidationConstants.MAX_NAME) String name,
    @NotBlank String role,
    @NotBlank String officeCode,
    String teamCode,
    @NotBlank @Size(max = ValidationConstants.MAX_PASSWORD) String password) {}
