package com.schoolmgmt.repository;

import com.schoolmgmt.model.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for StudentAttendance entity operations.
 */
@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, UUID> {

    /**
     * Find attendance record for a specific student on a specific date.
     */
    @Query("SELECT a FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate = :date AND a.tenantId = :tenantId")
    Optional<StudentAttendance> findByStudentIdAndDateAndTenantId(@Param("studentId") UUID studentId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Find all attendance records for a section on a specific date.
     */
    @Query("SELECT a FROM StudentAttendance a WHERE a.sectionId = :sectionId AND a.attendanceDate = :date AND a.tenantId = :tenantId")
    List<StudentAttendance> findBySectionIdAndDateAndTenantId(@Param("sectionId") UUID sectionId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Find all attendance records for a student in a date range.
     */
    @Query("SELECT a FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId ORDER BY a.attendanceDate")
    List<StudentAttendance> findByStudentIdAndDateRange(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Find all attendance records for a section in a date range.
     */
    @Query("SELECT a FROM StudentAttendance a WHERE a.sectionId = :sectionId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId ORDER BY a.attendanceDate")
    List<StudentAttendance> findBySectionIdAndDateRange(@Param("sectionId") UUID sectionId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Count present days for a student in a date range.
     */
    @Query("SELECT COUNT(a) FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId AND a.status IN ('PRESENT', 'LATE', 'HALF_DAY')")
    Long countPresentDays(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Count absent days for a student in a date range.
     */
    @Query("SELECT COUNT(a) FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId AND a.status = 'ABSENT'")
    Long countAbsentDays(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Count leave days for a student in a date range.
     */
    @Query("SELECT COUNT(a) FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId AND a.status = 'LEAVE'")
    Long countLeaveDays(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Check if attendance is already marked for a student on a date.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate = :date AND a.tenantId = :tenantId")
    boolean existsByStudentIdAndDateAndTenantId(@Param("studentId") UUID studentId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Check if attendance is marked for a section on a date.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM StudentAttendance a WHERE a.sectionId = :sectionId AND a.attendanceDate = :date AND a.tenantId = :tenantId")
    boolean existsBySectionIdAndDateAndTenantId(@Param("sectionId") UUID sectionId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Get attendance summary for a section on a specific date.
     */
    @Query("SELECT a.status, COUNT(a) FROM StudentAttendance a WHERE a.sectionId = :sectionId AND a.attendanceDate = :date AND a.tenantId = :tenantId GROUP BY a.status")
    List<Object[]> getAttendanceSummaryBySectionAndDate(@Param("sectionId") UUID sectionId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Find all attendance records for a student in an academic year.
     */
    @Query("SELECT a FROM StudentAttendance a WHERE a.studentId = :studentId AND a.academicYearId = :academicYearId AND a.tenantId = :tenantId ORDER BY a.attendanceDate")
    List<StudentAttendance> findByStudentIdAndAcademicYearId(@Param("studentId") UUID studentId, @Param("academicYearId") UUID academicYearId, @Param("tenantId") String tenantId);

    /**
     * Delete attendance record for a student on a specific date (for re-marking).
     */
    @Query("DELETE FROM StudentAttendance a WHERE a.studentId = :studentId AND a.attendanceDate = :date AND a.tenantId = :tenantId")
    void deleteByStudentIdAndDateAndTenantId(@Param("studentId") UUID studentId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);
}
