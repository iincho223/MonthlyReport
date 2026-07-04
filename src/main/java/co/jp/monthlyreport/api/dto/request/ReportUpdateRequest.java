package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import java.util.Map;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 月報更新リクエスト（API-09）。
 * インプット: 更新対象の月報ID と更新内容。
 * アウトプット: バリデーション済みの月報更新データ。
 *
 * @param reportId                 更新対象の月報ID（空不可）
 * @param month                    対象年月（yyyy-MM 形式、必須）
 * @param title                    タイトル（最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_REPORT_TITLE} 文字、必須）
 * @param salesInfo                売上・商談情報（任意）
 * @param nextMonthOvertimeHours   来月見込み残業時間（0〜{@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_OVERTIME_HOURS} 時間、必須）
 * @param nextMonthOvertimeReason  来月見込み残業理由（任意）
 * @param thisMonthOvertimeHours   今月実績残業時間（0〜{@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_OVERTIME_HOURS} 時間、必須）
 * @param thisMonthOvertimeReason  今月実績残業理由（任意）
 * @param conditions               体調コンディション（キー: physical/stress/relationships/worries/fatigue/sleep/motivation、値: BEST/GOOD/WARN/NG、必須）
 * @param comments                 コメント・相談事項（任意）
 */
public record ReportUpdateRequest(
    @NotBlank String reportId,
    @NotBlank @Pattern(regexp = ValidationConstants.REGEX_YEAR_MONTH, message = "must be yyyy-MM") String month,
    @NotBlank @Size(max = ValidationConstants.MAX_REPORT_TITLE) String title,
    String salesInfo,
    @NotNull @Min(ValidationConstants.MIN_OVERTIME_HOURS) @Max(ValidationConstants.MAX_OVERTIME_HOURS) Integer nextMonthOvertimeHours,
    String nextMonthOvertimeReason,
    @NotNull @Min(ValidationConstants.MIN_OVERTIME_HOURS) @Max(ValidationConstants.MAX_OVERTIME_HOURS) Integer thisMonthOvertimeHours,
    String thisMonthOvertimeReason,
    @NotNull Map<String, String> conditions,
    String comments) {}
