package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * エスカレーション詳細取得リクエスト（API-13）。
 * インプット: 取得対象のエスカレーションID。
 * アウトプット: バリデーション済みの詳細取得条件。
 *
 * @param escalationId エスカレーションID（必須）
 */
public record EscalationDetailRequest(
    @NotBlank String escalationId) {}
