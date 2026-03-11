package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.Pattern;

/**
 * ダッシュボード集計リクエスト（API-05）。
 * インプット: 集計対象の年月（省略可）。
 * アウトプット: バリデーション済みの年月条件。
 *
 * @param month 集計対象年月（yyyy-MM 形式、省略時は全期間を対象）
 */
public record DashboardSummaryRequest(@Pattern(regexp = ValidationConstants.REGEX_YEAR_MONTH_OPTIONAL, message = "{validation.year_month}") String month) {}
