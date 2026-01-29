package com.schoolmgmt.repository;

import com.schoolmgmt.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {

    List<LeaveType> findByTenantIdAndIsActiveTrue(String tenantId);

    List<LeaveType> findByTenantIdOrderByName(String tenantId);

    boolean existsByTenantIdAndName(String tenantId, String name);
}
