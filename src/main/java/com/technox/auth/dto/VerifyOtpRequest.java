package com.technox.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    @NotBlank(message = "Target email or mobile is required")
    private String target;

    @NotBlank(message = "OTP code is required")
    private String otp;
}
