package com.technox.attendance.dto;

import com.technox.attendance.entity.AttendanceStatus;
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
public class AttendanceCorrectionRequest {

    @NotNull(message = "New status is required")
    private AttendanceStatus newStatus;

    @NotBlank(message = "Reason for attendance correction is required")
    private String reason;
}
