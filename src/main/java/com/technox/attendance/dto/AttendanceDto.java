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
public class AttendanceDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String studentEmail;
    private String course;
    private String year;
    private Long registrationId;
    private String digitalPassId;
    private AttendanceStatus status;
    private LocalDateTime scannedAt;
    private Long scannedByUserId;
    private String operatorName;
    private String operatorRole;
    private boolean corrected;
}
