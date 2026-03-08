package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportDetailRequest(@NotBlank String reportId) {}
