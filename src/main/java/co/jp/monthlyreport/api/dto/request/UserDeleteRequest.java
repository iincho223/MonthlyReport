package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * ユーザー削除リクエスト（API-19）。
 * インプット: 削除対象のユーザーID。
 * アウトプット: バリデーション済みの削除対象データ。
 *
 * @param userId 削除対象のユーザーID（必須）
 */
public record UserDeleteRequest(@NotNull Long userId) {}
