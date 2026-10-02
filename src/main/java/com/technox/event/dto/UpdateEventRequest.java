package com.technox.event.dto;

import com.technox.event.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    private String title;
    private String description;
    private Long categoryId;
    private Long clubId;
    private String committeeCode;
    private String organizer;
    private String bannerUrl;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private Integer capacity;
    private LocalDateTime registrationDeadline;
    private String eligibility;
    private String rules;
    private String requirements;
    private EventStatus status;
    private String facultyCoordinator;
    private String committeeCoordinator;
}
