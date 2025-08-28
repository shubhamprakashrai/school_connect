package com.schoolmgmt.repository;

import com.schoolmgmt.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Student entity operations.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

    /**
     * Find student by roll number, class and tenant
     */
    Optional<Student> findByRollNumberAndCurrentClassIdAndTenantId(String rollNumber, String classId, String tenantId);

    /**
     * Find students by tenant ID
     */
    Page<Student> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find students by class and tenant
     */
    List<Student> findByCurrentClassIdAndTenantId(String classId, String tenantId);

    /**
     * Find students by class and section
     */
    List<Student> findByCurrentClassIdAndCurrentSectionIdAndTenantId(String classId, String sectionId, String tenantId);

    /**
     * Find students by status and tenant
     */
    List<Student> findByStatusAndTenantId(Student.StudentStatus status, String tenantId);

    /**
     * Find students by parent
     */
    @Query("SELECT s FROM Student s JOIN s.parents p WHERE p.id = :parentId")
    List<Student> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Find students by guardian
     */
    @Query("SELECT s FROM Student s JOIN s.guardians g WHERE g.id = :guardianId")
    List<Student> findByGuardianId(@Param("guardianId") UUID guardianId);

    /**
     * Check if roll number exists in class
     */
    boolean existsByRollNumberAndCurrentClassIdAndTenantId(String rollNumber, String classId, String tenantId);

    /**
     * Search students by name
     */
    @Query("SELECT s FROM Student s WHERE s.tenantId = :tenantId AND " +
           "(LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Student> searchStudents(@Param("searchTerm") String searchTerm, 
                                 @Param("tenantId") String tenantId, 
                                 Pageable pageable);

    /**
     * Find students by admission date range
     */
    List<Student> findByAdmissionDateBetweenAndTenantId(LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Count students by class and tenant
     */
    long countByCurrentClassIdAndTenantIdAndStatus(String classId, String tenantId, Student.StudentStatus status);

    /**
     * Count active students by tenant
     */
    long countByTenantIdAndStatus(String tenantId, Student.StudentStatus status);

    /**
     * Update student status
     */
    @Modifying
    @Query("UPDATE Student s SET s.status = :status WHERE s.id = :studentId")
    void updateStatus(@Param("studentId") UUID studentId, @Param("status") Student.StudentStatus status);

    /**
     * Find students with birthdays in date range
     */
    @Query("SELECT s FROM Student s WHERE s.tenantId = :tenantId AND " +
           "EXTRACT(MONTH FROM s.dateOfBirth) = :month AND " +
           "EXTRACT(DAY FROM s.dateOfBirth) BETWEEN :startDay AND :endDay")
    List<Student> findStudentsWithBirthdayInRange(@Param("tenantId") String tenantId,
                                                  @Param("month") int month,
                                                  @Param("startDay") int startDay,
                                                  @Param("endDay") int endDay);

    /**
     * Get student statistics by class
     */
    @Query("SELECT s.currentClassId, COUNT(s), " +
           "SUM(CASE WHEN s.gender = 'MALE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.gender = 'FEMALE' THEN 1 ELSE 0 END) " +
           "FROM Student s WHERE s.tenantId = :tenantId AND s.status = 'ACTIVE' " +
           "GROUP BY s.currentClassId")
    List<Object[]> getStudentStatisticsByClass(@Param("tenantId") String tenantId);

}
