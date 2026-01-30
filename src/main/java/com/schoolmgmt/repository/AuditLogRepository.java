package com.schoolmgmt.repository;

import com.schoolmgmt.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    Page<AuditLog> findByEntityTypeAndTenantIdAndIsActiveTrue(String entityType, String tenantId, Pageable pageable);
    Page<AuditLog> findByUserIdAndTenantIdAndIsActiveTrue(String userId, String tenantId, Pageable pageable);
    Page<AuditLog> findByActionAndTenantIdAndIsActiveTrue(String action, String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
}
