package com.technox.registration.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.event.entity.Event;
import com.technox.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_student", columnNames = {"event_id", "student_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {

    @Column(name = "registration_id", unique = true, nullable = false, length = 50)
    private String registrationId; // e.g. TX-2026-000001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "digital_pass_id", unique = true, length = 60)
    private String digitalPassId;

    @Column(name = "qr_token", unique = true, length = 120)
    private String qrToken;

    @Column(name = "pass_validity", length = 30)
    @Builder.Default
    private String passValidity = "VALID"; // VALID, USED, VOID

    @Column(name = "is_manual_override", nullable = false)
    @Builder.Default
    private boolean manualOverride = false;

    @Column(name = "override_reason", length = 255)
    private String overrideReason;

    @Column(name = "overridden_by_user_id")
    private Long overriddenByUserId;
}
