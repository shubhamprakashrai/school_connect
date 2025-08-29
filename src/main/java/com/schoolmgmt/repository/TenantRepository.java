package com.schoolmgmt.repository;

import com.schoolmgmt.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Tenant entity operations.
 * Provides methods for tenant management and queries.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * This is to find the max field from the database
     * @param
     * @return
     */
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(identifier, 3) AS INTEGER)), 0) + 1 " +
            "FROM tenants " +
            "WHERE identifier ~ '^..[0-9]+$'", nativeQuery = true)
    int getNextTenantSequence();







    /**
     * Find tenant by identifier
     * @param identifier The tenant identifier
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findByIdentifier(String identifier);

    /**
     * Find tenant by subdomain
     * @param subdomain The tenant subdomain
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findBySubdomain(String subdomain);

    /**
     * Find tenant by schema name
     * @param schemaName The database schema name
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * Find tenant by email
     * @param email The tenant email
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findByEmail(String email);

    /**
     * Find all active tenants
     * @return List of active tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE'")
    List<Tenant> findAllActive();

    /**
     * Find tenants by status
     * @param status The tenant status
     * @return List of tenants with the given status
     */
    List<Tenant> findByStatus(Tenant.TenantStatus status);

    /**
     * Find tenants by subscription plan
     * @param plan The subscription plan
     * @return List of tenants with the given plan
     */
    List<Tenant> findBySubscriptionPlan(Tenant.SubscriptionPlan plan);

    /**
     * Check if identifier exists
     * @param identifier The tenant identifier
     * @return true if exists, false otherwise
     */
    boolean existsByIdentifier(String identifier);

    /**
     * Check if subdomain exists
     * @param subdomain The subdomain
     * @return true if exists, false otherwise
     */
    boolean existsBySubdomain(String subdomain);

    /**
     * Check if schema name exists
     * @param schemaName The schema name
     * @return true if exists, false otherwise
     */
    boolean existsBySchemaName(String schemaName);

    /**
     * Check if email exists
     * @param email The email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Update tenant status
     * @param tenantId The tenant ID
     * @param status The new status
     * @param updatedBy The user making the update
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.status = :status, t.updatedBy = :updatedBy, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :tenantId")
    void updateStatus(@Param("tenantId") UUID tenantId, @Param("status") Tenant.TenantStatus status, @Param("updatedBy") String updatedBy);

    /**
     * Activate tenant
     * @param tenantId The tenant ID
     * @param activatedBy The user activating the tenant
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.status = 'ACTIVE', t.activatedAt = CURRENT_TIMESTAMP, t.updatedBy = :activatedBy WHERE t.id = :tenantId")
    void activateTenant(@Param("tenantId") UUID tenantId, @Param("activatedBy") String activatedBy);

    /**
     * Suspend tenant
     * @param tenantId The tenant ID
     * @param suspendedBy The user suspending the tenant
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.status = 'SUSPENDED', t.suspendedAt = CURRENT_TIMESTAMP, t.updatedBy = :suspendedBy WHERE t.id = :tenantId")
    void suspendTenant(@Param("tenantId") UUID tenantId, @Param("suspendedBy") String suspendedBy);

    /**
     * Soft delete tenant
     * @param tenantId The tenant ID
     * @param deletedBy The user deleting the tenant
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.status = 'DELETED', t.deletedAt = CURRENT_TIMESTAMP, t.updatedBy = :deletedBy WHERE t.id = :tenantId")
    void softDeleteTenant(@Param("tenantId") UUID tenantId, @Param("deletedBy") String deletedBy);

    /**
     * Update current student count
     * @param tenantId The tenant ID
     * @param delta The change in student count (positive or negative)
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.currentStudents = t.currentStudents + :delta WHERE t.id = :tenantId")
    void updateStudentCount(@Param("tenantId") UUID tenantId, @Param("delta") int delta);

    /**
     * Update current teacher count
     * @param tenantId The tenant ID
     * @param delta The change in teacher count (positive or negative)
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.currentTeachers = t.currentTeachers + :delta WHERE t.id = :tenantId")
    void updateTeacherCount(@Param("tenantId") UUID tenantId, @Param("delta") int delta);

    /**
     * Update current storage usage
     * @param tenantId The tenant ID
     * @param deltaMb The change in storage (positive or negative) in MB
     */
    @Modifying
    @Query("UPDATE Tenant t SET t.currentStorageMb = t.currentStorageMb + :deltaMb WHERE t.id = :tenantId")
    void updateStorageUsage(@Param("tenantId") UUID tenantId, @Param("deltaMb") int deltaMb);

    /**
     * Find tenants expiring soon (for trial plans)
     * @param expiryDate The expiry date threshold
     * @return List of tenants expiring before the given date
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionPlan = 'TRIAL' AND t.activatedAt < :expiryDate AND t.status = 'ACTIVE'")
    List<Tenant> findExpiringTrialTenants(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Get tenant statistics
     * @param tenantId The tenant ID
     * @return Tenant statistics as object array
     */
    @Query("SELECT t.currentStudents, t.maxStudents, t.currentTeachers, t.maxTeachers, t.currentStorageMb, t.maxStorageGb FROM Tenant t WHERE t.id = :tenantId")
    Object[] getTenantStatistics(@Param("tenantId") UUID tenantId);
}
