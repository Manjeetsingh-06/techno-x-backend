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
public class SendOtpRequest {
    @NotBlank(message = "Target email or mobile is required")
    private String target;
    private String type; // 'EMAIL' or 'MOBILE'
}
