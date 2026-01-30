package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AuditLog;
import com.schoolmgmt.repository.AuditLogRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLog create(AuditLog auditLog) {
        String tenantId = TenantContext.requireCurrentTenant();
        auditLog.setTenantId(tenantId);
        if (auditLog.getActionTimestamp() == null) {
            auditLog.setActionTimestamp(LocalDateTime.now());
        }
        AuditLog saved = repository.save(auditLog);
        log.info("Audit log created: {} {} {}", saved.getAction(), saved.getEntityType(), saved.getEntityId());
        return saved;
    }

    @Transactional(readOnly = true)
    public AuditLog getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        AuditLog auditLog = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        if (!auditLog.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("AuditLog", "id", id);
        }
        return auditLog;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByEntityType(String entityType, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByEntityTypeAndTenantIdAndIsActiveTrue(entityType, tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByUserId(String userId, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByUserIdAndTenantIdAndIsActiveTrue(userId, tenantId, pageable);
    }
}
