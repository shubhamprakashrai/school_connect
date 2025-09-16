package com.schoolmgmt.repository;

import com.schoolmgmt.model.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for TeacherSubject entity operations.
 * Manages the many-to-many relationship between Teachers and Subjects.
 */
@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, UUID> {

    /**
     * Find all subjects assigned to a teacher
     */
    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.tenantId = :tenantId")
    List<TeacherSubject> findByTeacherId(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Find all teachers assigned to a subject
     */
    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    List<TeacherSubject> findBySubjectId(@Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Check if teacher is assigned to subject
     */
    @Query("SELECT COUNT(ts) > 0 FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    boolean existsByTeacherIdAndSubjectId(@Param("teacherId") UUID teacherId, @Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Find specific assignment
     */
    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    TeacherSubject findByTeacherIdAndSubjectId(@Param("teacherId") UUID teacherId, @Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Assign teacher to subject
     */
    @Modifying
    @Query("INSERT INTO TeacherSubject (teacherId, subjectId, tenantId, isActive, createdAt) VALUES (:teacherId, :subjectId, :tenantId, true, CURRENT_TIMESTAMP)")
    void assignTeacherToSubject(@Param("teacherId") UUID teacherId, @Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Remove teacher from subject
     */
    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    void removeTeacherFromSubject(@Param("teacherId") UUID teacherId, @Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Deactivate teacher-subject assignment
     */
    @Modifying
    @Query("UPDATE TeacherSubject ts SET ts.isActive = false WHERE ts.teacherId = :teacherId AND ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    void deactivateAssignment(@Param("teacherId") UUID teacherId, @Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Get all active assignments for tenant
     */
    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.tenantId = :tenantId AND ts.isActive = true")
    List<TeacherSubject> findActiveAssignmentsByTenant(@Param("tenantId") String tenantId);

    /**
     * Remove all assignments for a teacher
     */
    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.tenantId = :tenantId")
    void removeAllAssignmentsForTeacher(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Remove all assignments for a subject
     */
    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.subjectId = :subjectId AND ts.tenantId = :tenantId")
    void removeAllAssignmentsForSubject(@Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Count teachers assigned to a subject
     */
    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.subjectId = :subjectId AND ts.tenantId = :tenantId AND ts.isActive = true")
    long countTeachersForSubject(@Param("subjectId") UUID subjectId, @Param("tenantId") String tenantId);

    /**
     * Count subjects assigned to a teacher
     */
    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.teacherId = :teacherId AND ts.tenantId = :tenantId AND ts.isActive = true")
    long countSubjectsForTeacher(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);
}