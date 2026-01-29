package com.schoolmgmt.repository;

import com.schoolmgmt.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findByTenantIdAndUserIdAndAcademicYear(
            String tenantId, String userId, String academicYear);

    Optional<LeaveBalance> findByTenantIdAndUserIdAndLeaveTypeIdAndAcademicYear(
            String tenantId, String userId, UUID leaveTypeId, String academicYear);

    List<LeaveBalance> findByTenantIdAndUserId(String tenantId, String userId);
}
