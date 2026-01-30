package com.schoolmgmt.repository;

import com.schoolmgmt.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID>, JpaSpecificationExecutor<Complaint> {
    Page<Complaint> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    Page<Complaint> findByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId, Pageable pageable);
    List<Complaint> findByFiledByIdAndTenantIdAndIsActiveTrue(String filedById, String tenantId);
    long countByTenantIdAndIsActiveTrue(String tenantId);
    long countByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId);
}
