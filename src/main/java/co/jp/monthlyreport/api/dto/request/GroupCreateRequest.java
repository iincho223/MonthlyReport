package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * グループ登録リクエスト（API-21）。
 * インプット: 新規グループの入力内容。
 * アウトプット: バリデーション済みのグループ登録データ。
 *
 * @param groupCode  グループコード（必須、英数字アンダースコア、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_CODE_LENGTH} 文字、一意）
 * @param groupName  グループ名（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_GROUP_TEAM_NAME} 文字）
 * @param officeCode 拠点コード（必須）
 * @param glUserId   グループリーダーに指定するユーザーID（必須）
 */
public record GroupCreateRequest(
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_CODE) @Size(max = ValidationConstants.MAX_CODE_LENGTH) String groupCode,
    @NotBlank @Size(max = ValidationConstants.MAX_GROUP_TEAM_NAME) String groupName,
    @NotBlank String officeCode,
    @NotNull Long glUserId) {}
