package com.technox.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long clubId;
    private String committeeCode;

    @NotBlank(message = "Organizer is required")
    private String organizer;

    private String bannerUrl;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Registration deadline is required")
    private LocalDateTime registrationDeadline;

    private String eligibility;
    private String rules;
    private String requirements;
    private String facultyCoordinator;
    private String committeeCoordinator;
}
