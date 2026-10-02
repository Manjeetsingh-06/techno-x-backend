package com.technox.audit.repository;

import com.technox.audit.entity.AuditAction;
import com.technox.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByActorUserId(Long actorUserId, Pageable pageable);

    Page<AuditLog> findByTargetType(String targetType, Pageable pageable);
}
