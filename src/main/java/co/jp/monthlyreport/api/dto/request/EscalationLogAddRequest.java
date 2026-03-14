package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 対応ログ追加リクエスト（API-16）。
 * インプット: 追記先のエスカレーションIDとログ本文。
 * アウトプット: バリデーション済みのログ追加データ。
 *
 * @param escalationId エスカレーションID（必須）
 * @param logText      対応ログ本文（必須、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_ESC_LOG_TEXT} 文字）
 */
public record EscalationLogAddRequest(
    @NotBlank String escalationId,
    @NotBlank @Size(max = ValidationConstants.MAX_ESC_LOG_TEXT) String logText) {}
