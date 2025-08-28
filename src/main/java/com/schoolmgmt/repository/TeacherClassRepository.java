package com.schoolmgmt.repository;

import com.schoolmgmt.model.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TeacherClass entity operations.
 */
@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, UUID>, JpaSpecificationExecutor<TeacherClass> {

    /**
     * Find active assignments for teacher
     */
    List<TeacherClass> findByTeacherIdAndIsActiveTrueAndTenantId(UUID teacherId, String tenantId);

    /**
     * Find all assignments for teacher (active and inactive)
     */
    List<TeacherClass> findByTeacherIdAndTenantId(UUID teacherId, String tenantId);

    /**
     * Find teachers assigned to a class
     */
    List<TeacherClass> findByClassIdAndIsActiveTrueAndTenantId(String classId, String tenantId);

    /**
     * Find teachers assigned to a specific class and section
     */
    List<TeacherClass> findByClassIdAndSectionIdAndIsActiveTrueAndTenantId(String classId, String sectionId, String tenantId);

    /**
     * Find class teacher for a class
     */
    Optional<TeacherClass> findByClassIdAndSectionIdAndIsClassTeacherTrueAndIsActiveTrueAndTenantId(String classId, String sectionId, String tenantId);

    /**
     * Find class teacher for a class (without section)
     */
    Optional<TeacherClass> findByClassIdAndIsClassTeacherTrueAndIsActiveTrueAndTenantId(String classId, String tenantId);

    /**
     * Check if teacher is assigned to class
     */
    boolean existsByTeacherIdAndClassIdAndSectionIdAndIsActiveTrueAndTenantId(UUID teacherId, String classId, String sectionId, String tenantId);

    /**
     * Check if teacher is assigned to class (without section check)
     */
    boolean existsByTeacherIdAndClassIdAndIsActiveTrueAndTenantId(UUID teacherId, String classId, String tenantId);

    /**
     * Find assignments by subject
     */
    List<TeacherClass> findBySubjectAndIsActiveTrueAndTenantId(String subject, String tenantId);

    /**
     * Find teacher assignment for specific class, section, and subject
     */
    Optional<TeacherClass> findByTeacherIdAndClassIdAndSectionIdAndSubjectAndTenantId(UUID teacherId, String classId, String sectionId, String subject, String tenantId);

    /**
     * Deactivate all assignments for a teacher
     */
    @Modifying
    @Query("UPDATE TeacherClass tc SET tc.isActive = false WHERE tc.teacherId = :teacherId AND tc.tenantId = :tenantId")
    void deactivateAllTeacherAssignments(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Deactivate assignment
     */
    @Modifying
    @Query("UPDATE TeacherClass tc SET tc.isActive = false WHERE tc.id = :assignmentId")
    void deactivateAssignment(@Param("assignmentId") UUID assignmentId);

    /**
     * Find active assignments by academic year
     */
    List<TeacherClass> findByAcademicYearAndIsActiveTrueAndTenantId(String academicYear, String tenantId);

    /**
     * Count active classes for teacher
     */
    long countByTeacherIdAndIsActiveTrueAndTenantId(UUID teacherId, String tenantId);

    /**
     * Get all unique classes assigned to teacher
     */
    @Query("SELECT DISTINCT tc.classId FROM TeacherClass tc WHERE tc.teacherId = :teacherId AND tc.isActive = true AND tc.tenantId = :tenantId")
    List<String> getAssignedClassesForTeacher(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Check if there's already a class teacher for this class
     */
    boolean existsByClassIdAndSectionIdAndIsClassTeacherTrueAndIsActiveTrueAndTenantId(String classId, String sectionId, String tenantId);

    /**
     * Find assignments that need to be renewed (for academic year transition)
     */
    @Query("SELECT tc FROM TeacherClass tc WHERE tc.academicYear = :oldAcademicYear AND tc.isActive = true AND tc.tenantId = :tenantId")
    List<TeacherClass> findAssignmentsForAcademicYearTransition(@Param("oldAcademicYear") String oldAcademicYear, @Param("tenantId") String tenantId);
}
