package com.schoolmgmt.repository;

import com.schoolmgmt.model.HostelRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HostelRoomRepository extends JpaRepository<HostelRoom, UUID>, JpaSpecificationExecutor<HostelRoom> {
    Page<HostelRoom> findByHostelIdAndTenantIdAndIsActiveTrue(String hostelId, String tenantId, Pageable pageable);
    List<HostelRoom> findByHostelIdAndStatusAndTenantIdAndIsActiveTrue(String hostelId, String status, String tenantId);
    long countByHostelIdAndTenantIdAndIsActiveTrue(String hostelId, String tenantId);
}
