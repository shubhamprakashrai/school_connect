package com.schoolmgmt.repository;

import com.schoolmgmt.model.HostelAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, UUID>, JpaSpecificationExecutor<HostelAllocation> {
    Page<HostelAllocation> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<HostelAllocation> findByStudentIdAndTenantIdAndIsActiveTrue(String studentId, String tenantId);
    List<HostelAllocation> findByRoomIdAndStatusAndTenantIdAndIsActiveTrue(String roomId, String status, String tenantId);
    long countByHostelIdAndStatusAndTenantIdAndIsActiveTrue(String hostelId, String status, String tenantId);
}
