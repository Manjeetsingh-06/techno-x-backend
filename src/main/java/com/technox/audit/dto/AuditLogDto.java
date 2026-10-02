package com.technox.audit.dto;

import com.technox.audit.entity.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private Long actorUserId;
    private String actorRole;
    private String actorEmail;
    private AuditAction action;
    private String targetType;
    private String targetId;
    private String reason;
    private String metadata;
    private String ipAddress;
    private LocalDateTime createdAt;
}
