package com.schoolmgmt.repository;

import com.schoolmgmt.model.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Repository interface for Parent entity operations.
 */
@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID>, JpaSpecificationExecutor<Parent> {

    /**
     * Find parent by email and tenant
     */
    Optional<Parent> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Find parent by phone and tenant
     */
    Optional<Parent> findByPhoneAndTenantId(String phone, String tenantId);

    /**
     * Find parents by tenant ID
     */
    Page<Parent> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find parents by status and tenant
     */
    List<Parent> findByStatusAndTenantId(Parent.ParentStatus status, String tenantId);

    /**
     * Find parents by type and tenant
     */
    List<Parent> findByParentTypeAndTenantId(Parent.ParentType parentType, String tenantId);

    /**
     * Find primary contacts
     */
    List<Parent> findByIsPrimaryContactTrueAndTenantId(String tenantId);

    /**
     * Find emergency contacts
     */
    List<Parent> findByIsEmergencyContactTrueAndTenantId(String tenantId);

    /**
     * Check if email exists
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Check if phone exists
     */
    boolean existsByPhoneAndTenantId(String phone, String tenantId);

    /**
     * Search parents by name
     */
    @Query("SELECT p FROM Parent p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Parent> searchParents(@Param("searchTerm") String searchTerm, 
                               @Param("tenantId") String tenantId, 
                               Pageable pageable);

    /**
     * Find parents of a student
     */
    @Query("SELECT p FROM Parent p JOIN p.children s WHERE s.id = :studentId")
    List<Parent> findParentsByStudentId(@Param("studentId") UUID studentId);

    /**
     * Find guardians of a student
     */
    @Query("SELECT p FROM Parent p JOIN p.wards s WHERE s.id = :studentId")
    List<Parent> findGuardiansByStudentId(@Param("studentId") UUID studentId);

    /**
     * Find all parents/guardians of a student
     */
    @Query("SELECT DISTINCT p FROM Parent p LEFT JOIN p.children c LEFT JOIN p.wards w " +
           "WHERE c.id = :studentId OR w.id = :studentId")
    List<Parent> findAllByStudentId(@Param("studentId") UUID studentId);

    /**
     * Count active parents by tenant
     */
    long countByTenantIdAndStatus(String tenantId, Parent.ParentStatus status);

    /**
     * Count parents by type
     */
    long countByParentTypeAndTenantId(Parent.ParentType parentType, String tenantId);

    /**
     * Update parent status
     */
    @Modifying
    @Query("UPDATE Parent p SET p.status = :status WHERE p.id = :parentId")
    void updateStatus(@Param("parentId") UUID parentId, @Param("status") Parent.ParentStatus status);

    /**
     * Update portal access
     */
    @Modifying
    @Query("UPDATE Parent p SET p.portalAccessEnabled = :enabled WHERE p.id = :parentId")
    void updatePortalAccess(@Param("parentId") UUID parentId, @Param("enabled") boolean enabled);

    /**
     * Update last portal login
     */
    @Modifying
    @Query("UPDATE Parent p SET p.lastPortalLogin = CURRENT_TIMESTAMP WHERE p.id = :parentId")
    void updateLastPortalLogin(@Param("parentId") UUID parentId);

    /**
     * Find parents who can pickup child
     */
    @Query("SELECT p FROM Parent p WHERE p.tenantId = :tenantId AND p.canPickupChild = true")
    List<Parent> findAuthorizedPickupParents(@Param("tenantId") String tenantId);

    /**
     * Find parents by communication preference
     */
    @Query("SELECT p FROM Parent p WHERE p.tenantId = :tenantId AND " +
           "(:sms = false OR p.receiveSms = true) AND " +
           "(:email = false OR p.receiveEmail = true) AND " +
           "(:app = false OR p.receiveAppNotifications = true)")
    List<Parent> findByCommunicationPreferences(@Param("tenantId") String tenantId,
                                                @Param("sms") boolean sms,
                                                @Param("email") boolean email,
                                                @Param("app") boolean app);

    /**
     * Get parent statistics
     */
    @Query("SELECT p.parentType, COUNT(p) FROM Parent p " +
           "WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE' " +
           "GROUP BY p.parentType")
    List<Object[]> getParentStatisticsByType(@Param("tenantId") String tenantId);

    /**
     * Find parents without portal access
     */
    @Query("SELECT p FROM Parent p WHERE p.tenantId = :tenantId AND " +
           "(p.user IS NULL OR p.portalAccessEnabled = false)")
    List<Parent> findParentsWithoutPortalAccess(@Param("tenantId") String tenantId);

    /**
     * Find inactive parents for cleanup
     */
    @Query("SELECT p FROM Parent p WHERE p.tenantId = :tenantId AND " +
           "p.status = 'INACTIVE' AND p.updatedAt < :cutoffDate")
    List<Parent> findInactiveParentsForCleanup(@Param("tenantId") String tenantId,
                                               @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Update parent as primary contact for student
     */
    @Modifying
    @Query("UPDATE Parent p SET p.isPrimaryContact = CASE WHEN p.id = :parentId THEN true ELSE false END " +
           "WHERE p IN (SELECT p2 FROM Parent p2 JOIN p2.children s WHERE s.id = :studentId)")
    void updatePrimaryContactForStudent(@Param("parentId") UUID parentId, @Param("studentId") UUID studentId);
}
