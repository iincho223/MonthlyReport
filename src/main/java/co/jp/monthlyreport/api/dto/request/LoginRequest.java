package co.jp.monthlyreport.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9]+$") @Size(max = 20) String employeeNo,
    @NotBlank @Size(max = 128) String password) {}
