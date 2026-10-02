package com.technox.attendance.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_corrections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCorrection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private AttendanceStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AttendanceStatus newStatus;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "corrected_by_user_id", nullable = false)
    private Long correctedByUserId;

    @Column(name = "corrected_by_name", length = 100)
    private String correctedByName;
}
