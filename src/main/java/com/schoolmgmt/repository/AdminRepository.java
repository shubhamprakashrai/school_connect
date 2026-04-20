package com.schoolmgmt.repository;

import com.schoolmgmt.model.Admin;
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
 * Repository interface for Admin entity operations.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID>, JpaSpecificationExecutor<Admin> {

    /**
     * Find admin by employee ID and tenant
     */
    Optional<Admin> findByEmployeeIdAndTenantId(String employeeId, String tenantId);

    /**
     * Find admin by employee ID (for current tenant)
     */
    Optional<Admin> findByEmployeeId(String employeeId);

    /**
     * Check if employee ID exists in tenant (including deleted records)
     * This is needed to avoid duplicate employee IDs even after soft delete
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM admins WHERE employee_id = :employeeId AND tenant_id = :tenantId", nativeQuery = true)
    boolean existsByEmployeeIdAndTenantIdIncludingDeleted(@Param("employeeId") String employeeId, @Param("tenantId") String tenantId);

    /**
     * Find admin by user ID
     */
    Optional<Admin> findByUserId(UUID userId);

    /**
     * Find user ID by admin ID (using foreign key)
     */
    @Query("SELECT a.user.id FROM Admin a WHERE a.id = :adminId")
    Optional<UUID> findUserIdByAdminId(@Param("adminId") UUID adminId);

    /**
     * Find user ID by admin ID using native query (fallback)
     */
    @Query(value = "SELECT user_id FROM admins WHERE id = :adminId", nativeQuery = true)
    Optional<UUID> findUserIdByAdminIdNative(@Param("adminId") UUID adminId);

    /**
     * Find admins by tenant ID
     */
    Page<Admin> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find admins by status and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.status = :status AND a.tenantId = :tenantId")
    Page<Admin> findByStatusAndTenantId(@Param("status") Admin.AdminStatus status, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find admins by department and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.department = :department AND a.tenantId = :tenantId")
    Page<Admin> findByDepartmentAndTenantId(@Param("department") String department, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find admins by designation and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.designation = :designation AND a.tenantId = :tenantId")
    Page<Admin> findByDesignationAndTenantId(@Param("designation") String designation, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find admins by status, department and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.status = :status AND a.department = :department AND a.tenantId = :tenantId")
    Page<Admin> findByStatusAndDepartmentAndTenantId(@Param("status") Admin.AdminStatus status, @Param("department") String department, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find admins by status, designation and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.status = :status AND a.designation = :designation AND a.tenantId = :tenantId")
    Page<Admin> findByStatusAndDesignationAndTenantId(@Param("status") Admin.AdminStatus status, @Param("designation") String designation, @Param("tenantId") String tenantId, Pageable pageable);

    /**
     * Find admins by status, department, designation and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.status = :status AND a.department = :department AND a.designation = :designation AND a.tenantId = :tenantId")
    Page<Admin> findByStatusAndDepartmentAndDesignationAndTenantId(
            @Param("status") Admin.AdminStatus status,
            @Param("department") String department,
            @Param("designation") String designation,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    /**
     * Find admin by ID and tenant
     */
    Optional<Admin> findByIdAndTenantId(UUID id, String tenantId);

    /**
     * Check if employee ID exists
     */
    boolean existsByEmployeeIdAndTenantId(String employeeId, String tenantId);

    /**
     * Check if employee ID exists globally (for super admin validation)
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Search admins by name
     */
    @Query("SELECT a FROM Admin a WHERE a.tenantId = :tenantId AND " +
           "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Admin> searchAdmins(@Param("searchTerm") String searchTerm,
                            @Param("tenantId") String tenantId,
                            Pageable pageable);

    /**
     * Find admins by employment type and tenant
     */
    Page<Admin> findByEmploymentTypeAndTenantId(Admin.EmploymentType employmentType, String tenantId, Pageable pageable);

    /**
     * Find admins by joining date range and tenant
     */
    @Query("SELECT a FROM Admin a WHERE a.tenantId = :tenantId AND " +
           "a.joinDate BETWEEN :startDate AND :endDate")
    Page<Admin> findByJoinDateRangeAndTenantId(@Param("tenantId") String tenantId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               Pageable pageable);

    /**
     * Count active admins by tenant
     */
    long countByTenantIdAndStatus(String tenantId, Admin.AdminStatus status);

    /**
     * Count admins by department and tenant
     */
    long countByDepartmentAndTenantIdAndStatus(String department, String tenantId, Admin.AdminStatus status);

    /**
     * Update admin status
     */
    @Query("UPDATE Admin a SET a.status = :status WHERE a.id = :adminId")
    void updateStatus(@Param("adminId") UUID adminId, @Param("status") Admin.AdminStatus status);

    /**
     * Find admins with birthdays in date range
     */
    @Query("SELECT a FROM Admin a WHERE a.tenantId = :tenantId AND " +
           "a.dateOfBirth IS NOT NULL AND " +
           "EXTRACT(MONTH FROM a.dateOfBirth) = :month AND " +
           "EXTRACT(DAY FROM a.dateOfBirth) BETWEEN :startDay AND :endDay")
    List<Admin> findAdminsWithBirthdayInRange(@Param("tenantId") String tenantId,
                                              @Param("month") int month,
                                              @Param("startDay") int startDay,
                                              @Param("endDay") int endDay);

    /**
     * Get admin statistics by department
     */
    @Query("SELECT a.department, COUNT(a), AVG(a.salary) " +
           "FROM Admin a WHERE a.tenantId = :tenantId AND a.status = 'ACTIVE' " +
           "GROUP BY a.department")
    List<Object[]> getAdminStatisticsByDepartment(@Param("tenantId") String tenantId);

    /**
     * Find admins by salary range
     */
    @Query("SELECT a FROM Admin a WHERE a.tenantId = :tenantId AND " +
           "a.salary BETWEEN :minSalary AND :maxSalary")
    Page<Admin> findBySalaryRange(@Param("tenantId") String tenantId,
                                  @Param("minSalary") java.math.BigDecimal minSalary,
                                  @Param("maxSalary") java.math.BigDecimal maxSalary,
                                  Pageable pageable);
}
