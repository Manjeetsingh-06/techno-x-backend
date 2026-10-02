package com.technox.audit.service;

import com.technox.audit.dto.AuditLogDto;
import com.technox.audit.entity.AuditAction;
import com.technox.audit.entity.AuditLog;
import com.technox.audit.repository.AuditLogRepository;
import com.technox.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAllLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        List<AuditLogDto> dtos = page.getContent().stream().map(this::mapToDto).toList();
        return PagedResponse.of(dtos, page);
    }

    @Transactional
    public void recordAction(Long actorId, String role, String email, AuditAction action, String targetType, String targetId, String reason, String metadata) {
        AuditLog log = AuditLog.builder()
                .actorUserId(actorId)
                .actorRole(role)
                .actorEmail(email)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .metadata(metadata)
                .build();

        auditLogRepository.save(log);
    }

    public AuditLogDto mapToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId())
                .actorRole(log.getActorRole())
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .reason(log.getReason())
                .metadata(log.getMetadata())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
