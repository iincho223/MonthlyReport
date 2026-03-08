package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackUpdateRequest(@NotBlank String reportId, @NotBlank @Size(max = 2000) String feedbackComment) {}
