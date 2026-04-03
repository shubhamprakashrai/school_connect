package com.schoolmgmt.repository;

import com.schoolmgmt.model.BillableStudentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface BillableStudentLogRepository extends JpaRepository<BillableStudentLog, UUID> {

    boolean existsByTenantIdAndStudentIdAndSnapshotDate(String tenantId, UUID studentId, LocalDate snapshotDate);

    @Query(value = "SELECT COUNT(*) FROM (" +
           "SELECT student_id FROM billable_student_logs " +
           "WHERE tenant_id = :tenantId AND snapshot_date BETWEEN :start AND :end AND is_active = true " +
           "GROUP BY student_id HAVING COUNT(DISTINCT snapshot_date) >= :minDays" +
           ") AS billable", nativeQuery = true)
    Long countBillableStudents(@Param("tenantId") String tenantId,
                               @Param("start") LocalDate start,
                               @Param("end") LocalDate end,
                               @Param("minDays") long minDays);

    @Query("SELECT COUNT(DISTINCT b.studentId) FROM BillableStudentLog b " +
           "WHERE b.tenantId = :tenantId AND b.snapshotDate BETWEEN :start AND :end AND b.isActive = true")
    long countDistinctStudentsInPeriod(@Param("tenantId") String tenantId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    void deleteBySnapshotDateBefore(LocalDate date);
}
