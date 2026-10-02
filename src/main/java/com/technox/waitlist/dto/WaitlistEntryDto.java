package com.technox.waitlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntryDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String studentEmail;
    private Integer position;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime promotedAt;
}
