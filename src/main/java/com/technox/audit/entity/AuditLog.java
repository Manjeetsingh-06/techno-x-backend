package com.technox.audit.entity;

import com.technox.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "actor_role", nullable = false, length = 50)
    private String actorRole;

    @Column(name = "actor_email", length = 150)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "target_type", length = 50)
    private String targetType; // e.g. "EVENT", "REGISTRATION", "STUDENT"

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON payload details

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}
