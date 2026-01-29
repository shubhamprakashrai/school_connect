package com.schoolmgmt.repository;

import com.schoolmgmt.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByTenantIdAndUserIdOrderByCreatedAtDesc(String tenantId, String userId);

    List<LeaveRequest> findByTenantIdAndStatusOrderByCreatedAtDesc(
            String tenantId, LeaveRequest.LeaveStatus status);

    Page<LeaveRequest> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.tenantId = :tenantId " +
           "AND lr.userId = :userId AND lr.status = 'APPROVED' " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("tenantId") String tenantId,
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr " +
           "WHERE lr.tenantId = :tenantId AND lr.userId = :userId " +
           "AND lr.leaveType.id = :leaveTypeId AND lr.status = 'APPROVED' " +
           "AND lr.startDate >= :yearStart AND lr.endDate <= :yearEnd")
    Integer getUsedDays(
            @Param("tenantId") String tenantId,
            @Param("userId") String userId,
            @Param("leaveTypeId") UUID leaveTypeId,
            @Param("yearStart") LocalDate yearStart,
            @Param("yearEnd") LocalDate yearEnd);

    List<LeaveRequest> findByTenantIdAndUserIdAndStatusIn(
            String tenantId, String userId, List<LeaveRequest.LeaveStatus> statuses);
}
