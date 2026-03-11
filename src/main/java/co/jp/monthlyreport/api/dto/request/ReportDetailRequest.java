package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 月報詳細取得リクエスト（API-07）。
 * インプット: 取得対象の月報ID。
 * アウトプット: バリデーション済みの月報ID。
 *
 * @param reportId 月報ID（空不可）
 */
public record ReportDetailRequest(@NotBlank String reportId) {}
