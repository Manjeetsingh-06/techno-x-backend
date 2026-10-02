package com.technox.event.entity;

import com.technox.category.entity.Category;
import com.technox.club.entity.Club;
import com.technox.common.entity.BaseEntity;
import com.technox.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Column(name = "uuid", unique = true, nullable = false, updatable = false, length = 36)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "slug", length = 150)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "committee_code", length = 50)
    private String committeeCode; // e.g. ABHIVYAKTI, KIRAN, OORJA, etc.

    @Column(name = "organizer", nullable = false, length = 100)
    private String organizer;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "venue", nullable = false, length = 150)
    private String venue;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "registered_count", nullable = false)
    @Builder.Default
    private Integer registeredCount = 0;

    @Column(name = "waitlist_count", nullable = false)
    @Builder.Default
    private Integer waitlistCount = 0;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "eligibility", length = 255)
    private String eligibility;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "faculty_coordinator", length = 100)
    private String facultyCoordinator;

    @Column(name = "committee_coordinator", length = 100)
    private String committeeCoordinator;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approval_reason", length = 255)
    private String approvalReason;

    @Version
    @Column(name = "version")
    private Long version;
}
