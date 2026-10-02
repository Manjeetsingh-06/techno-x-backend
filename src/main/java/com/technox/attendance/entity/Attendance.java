package com.technox.attendance.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.event.entity.Event;
import com.technox.registration.entity.Registration;
import com.technox.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_event_student", columnNames = {"event_id", "student_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "scanned_at", nullable = false)
    @Builder.Default
    private LocalDateTime scannedAt = LocalDateTime.now();

    @Column(name = "scanned_by_user_id")
    private Long scannedByUserId;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "operator_role", length = 50)
    private String operatorRole;

    @Column(name = "is_corrected", nullable = false)
    @Builder.Default
    private boolean corrected = false;
}
