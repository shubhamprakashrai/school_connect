package com.schoolmgmt.repository;

import com.schoolmgmt.model.Teacher;
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
 * Repository interface for Teacher entity operations.
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID>, JpaSpecificationExecutor<Teacher> {

    /**
     * Find teacher by employee ID and tenant
     */
    Optional<Teacher> findByEmployeeIdAndTenantId(String employeeId, String tenantId);

    /**
     * Find teacher by employee ID (for current tenant)
     */
    Optional<Teacher> findByEmployeeId(String employeeId);

    /**
     * Find teacher by email and tenant
     */
    Optional<Teacher> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Find teachers by tenant ID
     */
    Page<Teacher> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find teachers by department and tenant
     */
    List<Teacher> findByDepartmentAndTenantId(String department, String tenantId);

    /**
     * Find teachers by status and tenant
     */
    List<Teacher> findByStatusAndTenantId(Teacher.TeacherStatus status, String tenantId);

    /**
     * Find class teacher for a class
     */
    Optional<Teacher> findByClassTeacherForAndTenantId(String classId, String tenantId);

    /**
     * Check if employee ID exists
     */
    boolean existsByEmployeeIdAndTenantId(String employeeId, String tenantId);

    /**
     * Check if email exists
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Search teachers by name
     */
    @Query("SELECT t FROM Teacher t WHERE t.tenantId = :tenantId AND " +
           "(LOWER(t.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Teacher> searchTeachers(@Param("searchTerm") String searchTerm, 
                                 @Param("tenantId") String tenantId, 
                                 Pageable pageable);

    /**
     * Find teachers by subject
     */
    @Query("SELECT t FROM Teacher t JOIN t.subjects s WHERE s = :subject AND t.tenantId = :tenantId")
    List<Teacher> findBySubject(@Param("subject") String subject, @Param("tenantId") String tenantId);

    /**
     * Find teachers by class teacher assignment
     */
    @Query("SELECT t FROM Teacher t WHERE t.classTeacherFor = :classId AND t.tenantId = :tenantId")
    List<Teacher> findByClass(@Param("classId") String classId, @Param("tenantId") String tenantId);

    /**
     * Find teachers by qualification
     */
    @Query("SELECT t FROM Teacher t JOIN t.qualifications q WHERE q = :qualification AND t.tenantId = :tenantId")
    List<Teacher> findByQualification(@Param("qualification") String qualification, @Param("tenantId") String tenantId);

    /**
     * Find teachers by joining date range
     */
    List<Teacher> findByJoiningDateBetweenAndTenantId(LocalDate startDate, LocalDate endDate, String tenantId);

    /**
     * Count active teachers by tenant
     */
    long countByTenantIdAndStatus(String tenantId, Teacher.TeacherStatus status);

    /**
     * Count teachers by department
     */
    long countByDepartmentAndTenantIdAndStatus(String department, String tenantId, Teacher.TeacherStatus status);

    /**
     * Update teacher status
     */
    @Modifying
    @Query("UPDATE Teacher t SET t.status = :status WHERE t.id = :teacherId")
    void updateStatus(@Param("teacherId") UUID teacherId, @Param("status") Teacher.TeacherStatus status);

    /**
     * Assign class teacher
     */
    @Modifying
    @Query("UPDATE Teacher t SET t.isClassTeacher = true, t.classTeacherFor = :classId WHERE t.id = :teacherId")
    void assignClassTeacher(@Param("teacherId") UUID teacherId, @Param("classId") String classId);

    /**
     * Remove class teacher
     */
    @Modifying
    @Query("UPDATE Teacher t SET t.isClassTeacher = false, t.classTeacherFor = null WHERE t.id = :teacherId")
    void removeClassTeacher(@Param("teacherId") UUID teacherId);

    /**
     * Find teachers with birthdays in date range
     */
    @Query("SELECT t FROM Teacher t WHERE t.tenantId = :tenantId AND " +
           "EXTRACT(MONTH FROM t.dateOfBirth) = :month AND " +
           "EXTRACT(DAY FROM t.dateOfBirth) BETWEEN :startDay AND :endDay")
    List<Teacher> findTeachersWithBirthdayInRange(@Param("tenantId") String tenantId,
                                                  @Param("month") int month,
                                                  @Param("startDay") int startDay,
                                                  @Param("endDay") int endDay);

    /**
     * Get teacher statistics by department
     */
    @Query("SELECT t.department, COUNT(t), AVG(t.experienceYears), AVG(t.rating) " +
           "FROM Teacher t WHERE t.tenantId = :tenantId AND t.status = 'ACTIVE' " +
           "GROUP BY t.department")
    List<Object[]> getTeacherStatisticsByDepartment(@Param("tenantId") String tenantId);

    /**
     * Find teachers by employee type
     */
    List<Teacher> findByEmployeeTypeAndTenantId(Teacher.EmployeeType employeeType, String tenantId);

    /**
     * Find teachers with evaluation due
     */
    @Query("SELECT t FROM Teacher t WHERE t.tenantId = :tenantId AND " +
           "(t.lastEvaluationDate IS NULL OR t.lastEvaluationDate < :dueDate)")
    List<Teacher> findTeachersWithEvaluationDue(@Param("tenantId") String tenantId, 
                                                @Param("dueDate") LocalDate dueDate);

    /**
     * Update teacher rating
     */
    @Modifying
    @Query("UPDATE Teacher t SET t.rating = :rating, t.lastEvaluationDate = :evaluationDate " +
           "WHERE t.id = :teacherId")
    void updateRating(@Param("teacherId") UUID teacherId, 
                     @Param("rating") Double rating, 
                     @Param("evaluationDate") LocalDate evaluationDate);

    /**
     * Find teachers by salary range
     */
    @Query("SELECT t FROM Teacher t WHERE t.tenantId = :tenantId AND " +
           "t.grossSalary BETWEEN :minSalary AND :maxSalary")
    List<Teacher> findBySalaryRange(@Param("tenantId") String tenantId,
                                    @Param("minSalary") Double minSalary,
                                    @Param("maxSalary") Double maxSalary);
}
