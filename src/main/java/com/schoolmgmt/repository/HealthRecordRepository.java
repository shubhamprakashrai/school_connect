package com.schoolmgmt.repository;

import com.schoolmgmt.model.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID>, JpaSpecificationExecutor<HealthRecord> {
    Page<HealthRecord> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<HealthRecord> findByStudentIdAndTenantIdAndIsActiveTrue(String studentId, String tenantId);
    Page<HealthRecord> findByRecordTypeAndTenantIdAndIsActiveTrue(String recordType, String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
}
