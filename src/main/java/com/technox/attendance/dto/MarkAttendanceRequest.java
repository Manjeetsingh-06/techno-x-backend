package com.technox.attendance.dto;

import com.technox.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    // Either passToken (QR token or digitalPassId or studentId)
    private String token;
    private Long studentId;

    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;
}
