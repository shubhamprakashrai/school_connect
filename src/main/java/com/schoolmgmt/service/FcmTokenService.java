package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.FcmTokenRequest;
import com.schoolmgmt.model.FcmToken;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing FCM tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * Register or update FCM token for a user
     */
    public FcmToken registerToken(User user, FcmTokenRequest request) {
        log.info("Registering FCM token for user: {}", user.getUserId());

        // Check if token already exists
        Optional<FcmToken> existingToken = fcmTokenRepository.findByToken(request.getToken());

        if (existingToken.isPresent()) {
            FcmToken token = existingToken.get();
            // Update existing token ownership if different user
            if (!token.getUserId().equals(user.getUserId())) {
                token.setUserId(user.getUserId());
                token.setTenantId(user.getTenantId());
            }
            token.setIsActive(true);
            token.setLastUsedAt(LocalDateTime.now());
            token.setDeviceName(request.getDeviceName());
            token.setAppVersion(request.getAppVersion());

            FcmToken savedToken = fcmTokenRepository.save(token);
            log.info("Updated existing FCM token for user: {}", user.getUserId());
            return savedToken;
        }

        // Check if user has token for same device
        if (request.getDeviceId() != null) {
            Optional<FcmToken> deviceToken = fcmTokenRepository.findByUserIdAndDeviceId(
                user.getUserId(), request.getDeviceId());

            if (deviceToken.isPresent()) {
                FcmToken token = deviceToken.get();
                token.setToken(request.getToken());
                token.setIsActive(true);
                token.setLastUsedAt(LocalDateTime.now());
                token.setDeviceName(request.getDeviceName());
                token.setAppVersion(request.getAppVersion());

                FcmToken savedToken = fcmTokenRepository.save(token);
                log.info("Updated FCM token for existing device: {}", request.getDeviceId());
                return savedToken;
            }
        }

        // Create new token
        FcmToken newToken = FcmToken.builder()
                .userId(user.getUserId())
                .tenantId(user.getTenantId())
                .token(request.getToken())
                .deviceType(parseDeviceType(request.getDeviceType()))
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .appVersion(request.getAppVersion())
                .isActive(true)
                .lastUsedAt(LocalDateTime.now())
                .build();

        FcmToken savedToken = fcmTokenRepository.save(newToken);
        log.info("Created new FCM token for user: {}", user.getUserId());
        return savedToken;
    }

    /**
     * Remove FCM token
     */
    public void removeToken(String token) {
        log.info("Removing FCM token");
        fcmTokenRepository.deleteByToken(token);
    }

    /**
     * Deactivate FCM token
     */
    public void deactivateToken(String token) {
        log.info("Deactivating FCM token");
        fcmTokenRepository.deactivateToken(token);
    }

    /**
     * Remove all tokens for a user
     */
    public void removeAllTokensForUser(String userId) {
        log.info("Removing all FCM tokens for user: {}", userId);
        fcmTokenRepository.deleteByUserId(userId);
    }

    /**
     * Get all active tokens for a user
     */
    public List<FcmToken> getActiveTokensForUser(String userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Get all active tokens for a tenant
     */
    public List<FcmToken> getActiveTokensForTenant(String tenantId) {
        return fcmTokenRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed(String token) {
        fcmTokenRepository.updateLastUsed(token, LocalDateTime.now());
    }

    /**
     * Cleanup inactive tokens
     */
    public int cleanupInactiveTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<FcmToken> inactiveTokens = fcmTokenRepository.findInactiveTokens(cutoffDate);
        fcmTokenRepository.deleteAll(inactiveTokens);
        log.info("Cleaned up {} inactive FCM tokens", inactiveTokens.size());
        return inactiveTokens.size();
    }

    private FcmToken.DeviceType parseDeviceType(String deviceType) {
        if (deviceType == null) {
            return FcmToken.DeviceType.ANDROID;
        }
        try {
            return FcmToken.DeviceType.valueOf(deviceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FcmToken.DeviceType.ANDROID;
        }
    }
}
