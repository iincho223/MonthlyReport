package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * チーム登録リクエスト（API-24）。
 * インプット: 新規チームの入力内容。
 * アウトプット: バリデーション済みのチーム登録データ。
 *
 * @param teamCode  チームコード（必須、英数字アンダースコア、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_CODE_LENGTH} 文字、一意）
 * @param teamName  チーム名（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_GROUP_TEAM_NAME} 文字）
 * @param groupCode 所属グループコード（必須、既存グループ）
 * @param tlUserId  チームリーダーに指定するユーザーID（必須）
 */
public record TeamCreateRequest(
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_CODE) @Size(max = ValidationConstants.MAX_CODE_LENGTH) String teamCode,
    @NotBlank @Size(max = ValidationConstants.MAX_GROUP_TEAM_NAME) String teamName,
    @NotBlank String groupCode,
    @NotNull Long tlUserId) {}
