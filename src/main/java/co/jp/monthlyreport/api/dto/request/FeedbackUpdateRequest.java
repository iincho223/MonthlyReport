package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 回答更新リクエスト（API-11）。
 * インプット: 月報ID と回答コメント。
 * アウトプット: バリデーション済みの回答内容。
 *
 * @param reportId        回答対象の月報ID（空不可）
 * @param feedbackComment 回答コメント（空不可、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_FEEDBACK_COMMENT} 文字）
 */
public record FeedbackUpdateRequest(@NotBlank String reportId, @NotBlank @Size(max = ValidationConstants.MAX_FEEDBACK_COMMENT) String feedbackComment) {}
