package com.technox.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualRegistrationRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Override reason is required")
    private String reason;
}
