package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportDeleteRequest(@NotBlank String reportId) {}
