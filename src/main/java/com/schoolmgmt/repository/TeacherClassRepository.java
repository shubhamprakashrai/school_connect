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
     * Find all assignments for teacher
     */
    List<TeacherClass> findByTeacherId(UUID teacherId);

    /**
     * Find teachers assigned to a class
     */
    List<TeacherClass> findBySectionIdAndIsActiveTrueAndTenantId(UUID sectionId, String tenantId);


    /**
     * Check if teacher is assigned to class
     */
    boolean existsByTeacherIdAndSectionIdAndIsActiveTrueAndTenantId(UUID teacherId, UUID sectionId, String tenantId);

    /**
     * Check if teacher is assigned to class (without section check)
     */
    boolean existsByTeacherIdAndIsActiveTrueAndTenantId(UUID teacherId, String tenantId);

    /**
     * Find assignments by subject
     */
    List<TeacherClass> findBySubjectIdAndIsActiveTrueAndTenantId(UUID subjectId, String tenantId);

    /**
     * Find teacher assignment for specific class, section, and subject
     */
    Optional<TeacherClass> findByTeacherIdAndSectionIdAndSubjectIdAndTenantId(UUID teacherId, UUID sectionId, UUID subjectId, String tenantId);

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
    List<TeacherClass> findByAcademicYearIdAndIsActiveTrueAndTenantId(UUID academicYearId, String tenantId);

    /**
     * Count active classes for teacher
     */
    long countByTeacherIdAndIsActiveTrueAndTenantId(UUID teacherId, String tenantId);

    /**
     * Get all unique classes assigned to teacher
     */
    @Query("SELECT DISTINCT tc.sectionId FROM TeacherClass tc WHERE tc.teacherId = :teacherId AND tc.isActive = true AND tc.tenantId = :tenantId")
    List<UUID> getAssignedSectionsForTeacher(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Check if there's already a class teacher for this class
     */
    // Class teacher functionality not implemented in this entity

    /**
     * Count unique subjects taught by a teacher
     */
    @Query("SELECT COUNT(DISTINCT tc.subjectId) FROM TeacherClass tc WHERE tc.teacherId = :teacherId AND tc.isActive = true AND tc.tenantId = :tenantId")
    long countDistinctSubjectsByTeacherId(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    /**
     * Get all unique subject IDs taught by a teacher
     */
    @Query("SELECT DISTINCT tc.subjectId FROM TeacherClass tc WHERE tc.teacherId = :teacherId AND tc.isActive = true AND tc.tenantId = :tenantId")
    List<UUID> findDistinctSubjectIdsByTeacherId(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);
// Additional methods for assignment management
    boolean existsByTeacherIdAndSectionIdAndSubjectIdAndAcademicYearId(
            UUID teacherId, UUID sectionId, UUID subjectId, UUID academicYearId);


    
    List<TeacherClass> findBySectionIdAndTenantId(UUID sectionId, String tenantId);
    
    Optional<TeacherClass> findByIdAndTenantId(UUID id, String tenantId);
    
    int deleteByTeacherIdAndTenantId(UUID teacherId, String tenantId);

}
