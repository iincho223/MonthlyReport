package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.Pattern;

public record DashboardSummaryRequest(@Pattern(regexp = "^$|^\\d{4}-\\d{2}$", message = "must be yyyy-MM") String month) {}
