package com.schoolmgmt.repository;

import com.schoolmgmt.model.Hostel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, UUID>, JpaSpecificationExecutor<Hostel> {
    Page<Hostel> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
}
