package com.schoolmgmt.repository;

import com.schoolmgmt.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username and tenant ID
     */
    Optional<User> findByUsernameAndTenantId(String username, String tenantId);



    /**
     * Find user by email and tenant ID
     */
    Optional<User> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Find user by username or email and tenant ID
     */
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.tenantId = :tenantId")
    Optional<User> findByUsernameOrEmailAndTenantId(@Param("usernameOrEmail") String usernameOrEmail, @Param("tenantId") String tenantId);

    /**
     * Find user by username or email across all tenants
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Find user by email across all tenants
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Check if username exists for a tenant
     */
    // Optional: check if username already exists for a tenant
    boolean existsByUsernameAndTenantId(String username, String tenantId);

    // Get the max sequence number for a tenant prefix
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(username, LENGTH(:tenantPrefix) + 1) AS INTEGER)), 0) " +
            "FROM users WHERE username LIKE CONCAT(:tenantPrefix, '%')", nativeQuery = true)
    Integer findMaxSequenceForTenant(@Param("tenantPrefix") String tenantPrefix);


    /**
     * Check if email exists
     * @param email The email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone exists
     * @param phone The phone
     * @return true if exists, false otherwise
     */
    boolean existsByPhone(String phone);


    /**
     * Check if email exists for a tenant
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Find users by tenant ID
     */
    List<User> findByTenantId(String tenantId);

    /**
     * Find users by tenant ID with pagination
     */
    Page<User> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find users by role and tenant ID
     */
    List<User> findByRoleAndTenantId(User.UserRole role, String tenantId);

    /**
     * Find users by status and tenant ID
     */
    List<User> findByStatusAndTenantId(User.UserStatus status, String tenantId);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find users with expired password reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetTokenExpiry < :now AND u.passwordResetToken IS NOT NULL")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Update failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Reset failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = NULL WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Lock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") UUID userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    /**
     * Unlock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true, u.lockedUntil = NULL WHERE u.id = :userId")
    void unlockUserAccount(@Param("userId") UUID userId);

    /**
     * Update user password
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.lastPasswordChangeAt = :changeTime, u.temporaryPassword=true ,u.passwordResetToken = NULL, u.passwordResetTokenExpiry = NULL WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("password") String password, @Param("changeTime") LocalDateTime changeTime);

    /**
     * Set password reset token
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = :token, u.passwordResetTokenExpiry = :expiry WHERE u.id = :userId")
    void setPasswordResetToken(@Param("userId") UUID userId, @Param("token") String token, @Param("expiry") LocalDateTime expiry);

    /**
     * Set Initial password reset token
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE User u " +
            "SET u.password = :password, " +
            "u.temporaryPassword = true, " +
            "u.lastPasswordChangeAt = :changedAt " +
            "WHERE u.id = :userId")
    int setInitialPasswordReset(@Param("userId") UUID userId,
                       @Param("password") String password,
                       @Param("changedAt") LocalDateTime changedAt);



//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Transactional
//    @Query("UPDATE User u SET u.password = :password, " +
//            "u.passwordResetToken = null, " +
//            "u.passwordResetTokenExpiry = null, " +
//            "u.temporaryPassword = false, " +
//            "u.lastPasswordChangeAt = :changedAt " +
//            "WHERE u.id = :userId")
//    int setInitialPasswordReset(@Param("userId") UUID userId,
//                       @Param("password") String password,
//                       @Param("changedAt") LocalDateTime changedAt);




    /**
     * Verify email
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = NULL, u.status = 'ACTIVE' WHERE u.id = :userId")
    void verifyEmail(@Param("userId") UUID userId);

    /**
     * Update MFA secret
     */
    @Modifying
    @Query("UPDATE User u SET u.mfaSecret = :secret, u.mfaEnabled = :enabled WHERE u.id = :userId")
    void updateMfaSettings(@Param("userId") UUID userId, @Param("secret") String secret, @Param("enabled") boolean enabled);

    /**
     * Count users by role and tenant
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.tenantId = :tenantId AND u.status = 'ACTIVE'")
    long countActiveUsersByRoleAndTenant(@Param("role") User.UserRole role, @Param("tenantId") String tenantId);

    /**
     * Find users by reference ID and type
     */
    Optional<User> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    /**
     * Find inactive users for cleanup
     */
    @Query("SELECT u FROM User u WHERE u.status = 'PENDING' AND u.createdAt < :cutoffDate")
    List<User> findInactiveUsersForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
}
