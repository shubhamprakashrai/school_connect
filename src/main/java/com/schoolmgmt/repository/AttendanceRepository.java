package com.schoolmgmt.repository;

import com.schoolmgmt.model.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Attendance entity operations.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    /**
     * Find attendance by student and date
     */
    Optional<Attendance> findByStudentIdAndAttendanceDateAndTenantId(UUID studentId, LocalDate date, String tenantId);

    /**
     * Find attendance by class and date
     */
    List<Attendance> findByClassIdAndAttendanceDateAndTenantId(String classId, LocalDate date, String tenantId);

    /**
     * Find attendance by class, section and date
     */
    List<Attendance> findByClassIdAndSectionIdAndAttendanceDateAndTenantId(String classId, String sectionId, LocalDate date, String tenantId);

    /**
     * Find attendance by student for date range
     */
    List<Attendance> findByStudentIdAndAttendanceDateBetweenAndTenantId(UUID studentId, LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Find attendance by class for date range
     */
    List<Attendance> findByClassIdAndAttendanceDateBetweenAndTenantId(String classId, LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Find attendance by teacher
     */
    List<Attendance> findByMarkedByTeacherIdAndTenantId(UUID teacherId, String tenantId);

    /**
     * Check if attendance exists for student on date
     */
    boolean existsByStudentIdAndAttendanceDateAndTenantId(UUID studentId, LocalDate date, String tenantId);

    /**
     * Count attendance by status for class on specific date
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.classId = :classId AND a.attendanceDate = :date AND a.tenantId = :tenantId GROUP BY a.status")
    List<Object[]> countAttendanceByStatusForClass(@Param("classId") String classId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Get attendance statistics for class in date range
     */
    @Query("SELECT a.attendanceDate, a.status, COUNT(a) FROM Attendance a WHERE a.classId = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId GROUP BY a.attendanceDate, a.status ORDER BY a.attendanceDate")
    List<Object[]> getAttendanceStatisticsForClass(@Param("classId") String classId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get student attendance summary
     */
    @Query("SELECT a.studentId, a.status, COUNT(a) FROM Attendance a WHERE a.classId = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId GROUP BY a.studentId, a.status")
    List<Object[]> getStudentAttendanceSummary(@Param("classId") String classId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get attendance by status
     */
    List<Attendance> findByStatusAndTenantId(Attendance.AttendanceStatus status, String tenantId);

    /**
     * Find attendance by subject
     */
    List<Attendance> findBySubjectAndTenantId(String subject, String tenantId);

    /**
     * Get total attendance count for student in date range
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId")
    long getTotalAttendanceCountForStudent(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get present attendance count for student in date range
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.status = 'PRESENT' AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId")
    long getPresentAttendanceCountForStudent(@Param("studentId") UUID studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get attendance percentage for class
     */
    @Query("SELECT (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) FROM Attendance a WHERE a.classId = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId")
    Double getAttendancePercentageForClass(@Param("classId") String classId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Find students with low attendance
     */
    @Query("SELECT a.studentId, (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) as attendance_percentage " +
           "FROM Attendance a WHERE a.classId = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId " +
           "GROUP BY a.studentId HAVING attendance_percentage < :threshold")
    List<Object[]> findStudentsWithLowAttendance(@Param("classId") String classId, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate, 
                                                 @Param("threshold") Double threshold, 
                                                 @Param("tenantId") String tenantId);
}
