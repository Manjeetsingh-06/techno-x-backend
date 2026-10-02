package com.technox.waitlist.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.event.entity.Event;
import com.technox.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "waitlist_entries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_waitlist_event_student", columnNames = {"event_id", "student_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "WAITLISTED"; // WAITLISTED, PROMOTED, CANCELLED

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    @Column(name = "promoted_by_user_id")
    private Long promotedByUserId;
}
