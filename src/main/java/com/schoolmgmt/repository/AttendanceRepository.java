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
     * Find attendance by student ID (paginated)
     */
    Page<Attendance> findByStudentId(UUID studentId, Pageable pageable);

    /**
     * Find attendance by date (paginated)
     */
    Page<Attendance> findByAttendanceDate(LocalDate date, Pageable pageable);

    /**
     * Find attendance by student ID and date range
     */
    List<Attendance> findByStudentIdAndAttendanceDateBetween(UUID studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Find attendance by teacher class section ID (paginated)
     */
    @Query("SELECT a FROM Attendance a JOIN a.teacherClass tc WHERE tc.sectionId = :sectionId")
    Page<Attendance> findByTeacherClass_SectionId(@Param("sectionId") UUID sectionId, Pageable pageable);

    /**
     * Find attendance by class and date
     */
    List<Attendance> findByTeacherClassIdAndAttendanceDateAndTenantId(UUID teacherClassId, LocalDate date, String tenantId);

    /**
     * Find attendance by class, section and date (Note: attendance doesn't have sectionId, removing this method)
     */

    /**
     * Find attendance by student for date range
     */
    List<Attendance> findByStudentIdAndAttendanceDateBetweenAndTenantId(UUID studentId, LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Find attendance by class for date range
     */
    List<Attendance> findByTeacherClassIdAndAttendanceDateBetweenAndTenantId(UUID teacherClassId, LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Find attendance by teacher
     */
    @Query("SELECT a FROM Attendance a JOIN a.teacherClass tc WHERE tc.teacherId = :teacherId AND a.tenantId = :tenantId")
    List<Attendance> findByMarkedByTeacherIdAndTenantId(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Check if attendance exists for student on date
     */
    boolean existsByStudentIdAndAttendanceDateAndTenantId(UUID studentId, LocalDate date, String tenantId);

    /**
     * Count attendance by status for class on specific date
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.teacherClassId = :teacherClassId AND a.attendanceDate = :date AND a.tenantId = :tenantId GROUP BY a.status")
    List<Object[]> countAttendanceByStatusForClass(@Param("teacherClassId") UUID teacherClassId, @Param("date") LocalDate date, @Param("tenantId") String tenantId);

    /**
     * Get attendance statistics for class in date range
     */
    @Query("SELECT a.attendanceDate, a.status, COUNT(a) FROM Attendance a WHERE a.teacherClassId = :teacherClassId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId GROUP BY a.attendanceDate, a.status ORDER BY a.attendanceDate")
    List<Object[]> getAttendanceStatisticsForClass(@Param("teacherClassId") UUID teacherClassId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get student attendance summary
     */
    @Query("SELECT a.studentId, a.status, COUNT(a) FROM Attendance a WHERE a.teacherClassId = :teacherClassId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId GROUP BY a.studentId, a.status")
    List<Object[]> getStudentAttendanceSummary(@Param("teacherClassId") UUID teacherClassId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Get attendance by status
     */
    List<Attendance> findByStatusAndTenantId(Attendance.AttendanceStatus status, String tenantId);

    /**
     * Find attendance by subject
     */
    @Query("SELECT a FROM Attendance a JOIN a.teacherClass tc JOIN tc.subject s WHERE s.name = :subject AND a.tenantId = :tenantId")
    List<Attendance> findBySubjectAndTenantId(@Param("subject") String subject, @Param("tenantId") String tenantId);

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
    @Query("SELECT (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) FROM Attendance a WHERE a.teacherClassId = :teacherClassId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId")
    Double getAttendancePercentageForClass(@Param("teacherClassId") UUID teacherClassId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("tenantId") String tenantId);

    /**
     * Find students with low attendance
     */
    @Query("SELECT a.studentId, (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) as attendance_percentage " +
           "FROM Attendance a WHERE a.teacherClassId = :teacherClassId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.tenantId = :tenantId " +
           "GROUP BY a.studentId HAVING (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) < :threshold")
    List<Object[]> findStudentsWithLowAttendance(@Param("teacherClassId") UUID teacherClassId, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate, 
                                                 @Param("threshold") Double threshold, 
                                                 @Param("tenantId") String tenantId);
}
