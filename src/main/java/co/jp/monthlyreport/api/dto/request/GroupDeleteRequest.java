package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * グループ削除リクエスト（API-22）。
 * インプット: 削除対象のグループコード。
 * アウトプット: バリデーション済みの削除対象データ。
 *
 * @param groupCode 削除対象のグループコード（必須）
 */
public record GroupDeleteRequest(@NotBlank String groupCode) {}
