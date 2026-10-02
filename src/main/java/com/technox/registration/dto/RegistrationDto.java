package com.technox.registration.dto;

import com.technox.registration.entity.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto {
    private Long id;
    private String registrationId;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private String eventVenue;
    private String eventCategory;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String studentEmail;
    private String course;
    private String year;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
    private String digitalPassId;
    private String qrToken;
    private String passValidity;
    private boolean manualOverride;
    private String overrideReason;
    private Long overriddenByUserId;
}
