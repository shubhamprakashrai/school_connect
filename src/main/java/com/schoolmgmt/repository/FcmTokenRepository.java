package com.schoolmgmt.repository;

import com.schoolmgmt.model.FcmToken;
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
 * Repository for FCM Token operations.
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {

    /**
     * Find token by token string
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * Find all tokens for a user
     */
    List<FcmToken> findByUserIdAndIsActiveTrue(String userId);

    /**
     * Find all tokens for a tenant
     */
    List<FcmToken> findByTenantIdAndIsActiveTrue(String tenantId);

    /**
     * Find token by user and device
     */
    Optional<FcmToken> findByUserIdAndDeviceId(String userId, String deviceId);

    /**
     * Check if token exists
     */
    boolean existsByToken(String token);

    /**
     * Delete token by token string
     */
    void deleteByToken(String token);

    /**
     * Delete all tokens for a user
     */
    void deleteByUserId(String userId);

    /**
     * Deactivate token
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.token = :token")
    void deactivateToken(@Param("token") String token);

    /**
     * Update last used timestamp
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.lastUsedAt = :lastUsedAt WHERE f.token = :token")
    void updateLastUsed(@Param("token") String token, @Param("lastUsedAt") LocalDateTime lastUsedAt);

    /**
     * Find inactive tokens for cleanup
     */
    @Query("SELECT f FROM FcmToken f WHERE f.lastUsedAt < :cutoffDate OR f.isActive = false")
    List<FcmToken> findInactiveTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active tokens for a user
     */
    long countByUserIdAndIsActiveTrue(String userId);
}
