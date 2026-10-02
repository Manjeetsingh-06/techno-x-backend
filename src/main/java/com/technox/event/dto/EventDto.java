package com.technox.event.dto;

import com.technox.category.dto.CategoryDto;
import com.technox.club.dto.ClubDto;
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
public class EventDto {
    private Long id;
    private String uuid;
    private String title;
    private String slug;
    private String description;
    private CategoryDto category;
    private ClubDto club;
    private String committeeCode;
    private String organizer;
    private String bannerUrl;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private Integer capacity;
    private Integer registeredCount;
    private Integer waitlistCount;
    private LocalDateTime registrationDeadline;
    private String eligibility;
    private String rules;
    private String requirements;
    private EventStatus status;
    private Long createdByUserId;
    private String creatorName;
    private String facultyCoordinator;
    private String committeeCoordinator;
    private Long approvedByUserId;
    private LocalDateTime approvedAt;
    private String approvalReason;
    private LocalDateTime createdAt;
}
