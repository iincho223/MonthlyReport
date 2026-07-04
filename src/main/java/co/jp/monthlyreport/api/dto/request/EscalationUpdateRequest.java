package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * エスカレーション更新リクエスト（API-15）。
 * インプット: 更新対象のエスカレーションIDと更新内容。
 * アウトプット: バリデーション済みのエスカレーション更新データ。
 *
 * @param escalationId        更新対象のエスカレーションID（必須）
 * @param title               案件タイトル（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_ESC_TITLE} 文字）
 * @param targetEmployeeName  対象メンバー氏名（必須）
 * @param targetTeam          対象チーム名（必須）
 * @param description         詳細内容（任意、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_ESC_DESCRIPTION} 文字）
 * @param severity            重要度（LOW / MEDIUM / HIGH。必須）
 * @param status              ステータス（PENDING / ONGOING / RESOLVED。必須）
 * @param dueDate             対応期日（yyyy-MM-dd 形式、必須）
 * @param resolvedDate        完了期日（yyyy-MM-dd 形式。status=RESOLVED の場合必須）
 * @param assigneeUserId      担当者ユーザーID（未設定への変更は不可）
 */
public record EscalationUpdateRequest(
    @NotBlank String escalationId,
    @NotBlank @Size(max = ValidationConstants.MAX_ESC_TITLE) String title,
    @NotBlank String targetEmployeeName,
    @NotBlank String targetTeam,
    @Size(max = ValidationConstants.MAX_ESC_DESCRIPTION) String description,
    @NotBlank String severity,
    @NotBlank String status,
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_DATE, message = "must be yyyy-MM-dd") String dueDate,
    String resolvedDate,
    Long assigneeUserId) {}
