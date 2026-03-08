package co.jp.monthlyreport.api.dto.request;

import java.util.Map;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReportUpdateRequest(
    @NotBlank String reportId,
    @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "must be yyyy-MM") String month,
    @NotBlank @Size(max = 100) String title,
    String salesInfo,
    @NotNull @Min(0) @Max(300) Integer nextMonthOvertimeHours,
    String nextMonthOvertimeReason,
    @NotNull @Min(0) @Max(300) Integer thisMonthOvertimeHours,
    String thisMonthOvertimeReason,
    @NotNull Map<String, String> conditions,
    String comments) {}
