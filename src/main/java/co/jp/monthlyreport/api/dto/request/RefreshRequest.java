package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * トークン再発行リクエスト（API-02）。
 * インプット: 有効なリフレッシュトークン。
 * アウトプット: バリデーション済みのトークン文字列。
 *
 * @param refreshToken リフレッシュトークン（空不可）
 */
public record RefreshRequest(@NotBlank String refreshToken) {}
