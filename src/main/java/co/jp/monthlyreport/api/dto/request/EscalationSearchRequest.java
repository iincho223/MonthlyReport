package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.Pattern;

/**
 * エスカレーション一覧検索リクエスト（API-12）。
 * インプット: 絞り込み条件（status、ページング）。
 * アウトプット: バリデーション済みの検索条件。
 *
 * @param status ステータス絞り込み（ALL / PENDING / ONGOING / RESOLVED。省略時 ALL）
 * @param page   ページ番号（省略時 1）
 * @param size   1ページあたりの件数（省略時 20）
 */
public record EscalationSearchRequest(
    @Pattern(regexp = "^$|^(ALL|PENDING|ONGOING|RESOLVED)$", message = "ALL/PENDING/ONGOING/RESOLVED のいずれかを指定してください")
    String status,
    Integer page,
    Integer size) {}
