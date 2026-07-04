package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * チーム削除リクエスト（API-25）。
 * インプット: 削除対象のチームコード。
 * アウトプット: バリデーション済みの削除対象データ。
 *
 * @param teamCode 削除対象のチームコード（必須）
 */
public record TeamDeleteRequest(@NotBlank String teamCode) {}
