package co.jp.monthlyreport.api.dto.request;

import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ReportSearchRequest(
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "must be yyyy-MM") String month,
    String status,
    @Min(1) Integer page,
    @Min(1) @Max(100) Integer size,
    List<String> sort) {}
