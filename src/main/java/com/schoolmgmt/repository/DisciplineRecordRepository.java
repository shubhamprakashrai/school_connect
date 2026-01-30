package com.schoolmgmt.repository;

import com.schoolmgmt.model.DisciplineRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisciplineRecordRepository extends JpaRepository<DisciplineRecord, UUID>, JpaSpecificationExecutor<DisciplineRecord> {
    Page<DisciplineRecord> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<DisciplineRecord> findByStudentIdAndTenantIdAndIsActiveTrue(String studentId, String tenantId);
    Page<DisciplineRecord> findByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
    long countByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId);
}
