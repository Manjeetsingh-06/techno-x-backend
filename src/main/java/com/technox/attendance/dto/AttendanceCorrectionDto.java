package com.technox.attendance.dto;

import com.technox.attendance.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionDto {
    private Long id;
    private Long attendanceId;
    private AttendanceStatus previousStatus;
    private AttendanceStatus newStatus;
    private String reason;
    private Long correctedByUserId;
    private String correctedByName;
    private LocalDateTime createdAt;
}
