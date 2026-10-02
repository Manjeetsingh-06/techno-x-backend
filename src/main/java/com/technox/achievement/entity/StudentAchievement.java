package com.technox.achievement.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_achievement", columnNames = {"student_id", "achievement_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAchievement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "earned_at", nullable = false)
    @Builder.Default
    private LocalDateTime earnedAt = LocalDateTime.now();

    @Column(name = "associated_event_title", length = 150)
    private String associatedEventTitle;
}
