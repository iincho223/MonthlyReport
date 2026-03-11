package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 月報削除リクエスト（API-10）。
 * インプット: 削除対象の月報ID。
 * アウトプット: バリデーション済みの月報ID。
 *
 * @param reportId 月報ID（空不可）
 */
public record ReportDeleteRequest(@NotBlank String reportId) {}
