package com.schoolmgmt.infrastructure.security;

import com.schoolmgmt.domain.user.User;
import com.schoolmgmt.domain.user.UserRepository;
import com.schoolmgmt.infrastructure.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from database considering tenant context.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            log.error("No tenant context set while loading user: {}", username);
            throw new UsernameNotFoundException("No tenant context available. Please ensure you're providing tenant information.");
        }

        log.debug("Loading user: {} for tenant: {}", username, tenantId);

        try {
            // Try to find by username or email
            User user = userRepository.findByUsernameOrEmailAndTenantId(username, tenantId)
                    .orElseThrow(() -> {
                        log.error("User not found: {} in tenant: {}", username, tenantId);
                        return new UsernameNotFoundException("User not found: " + username);
                    });

            // Check if user account is active
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Inactive user attempting to login: {} in tenant: {}", username, tenantId);
                throw new UsernameNotFoundException("User account is not active: " + username);
            }

            log.debug("User loaded successfully: {} with role: {} in tenant: {}",
                    user.getUsername(), user.getPrimaryRole(), tenantId);

            return user;

        } catch (Exception e) {
            log.error("Error loading user: {} in tenant: {}", username, tenantId, e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }

    /**
     * Load user by ID (useful for token validation)
     */
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        try {
            User user = userRepository.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new UsernameNotFoundException("User not found with ID: " + userId);
                    });

            log.debug("User loaded by ID: {} with username: {}", userId, user.getUsername());
            return user;

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for user ID: {}", userId);
            throw new UsernameNotFoundException("Invalid user ID format: " + userId);
        } catch (Exception e) {
            log.error("Error loading user by ID: {}", userId, e);
            throw new UsernameNotFoundException("Error loading user with ID: " + userId, e);
        }
    }

    /**
     * Load user by username with explicit tenant ID (for special cases)
     */
    public UserDetails loadUserByUsernameAndTenant(String username, String tenantId) throws UsernameNotFoundException {
        log.debug("Loading user: {} for explicit tenant: {}", username, tenantId);

        try {
            User user = userRepository.findByUsernameOrEmailAndTenantId(username, tenantId)
                    .orElseThrow(() -> {
                        log.error("User not found: {} in tenant: {}", username, tenantId);
                        return new UsernameNotFoundException("User not found: " + username);
                    });

            log.debug("User loaded with explicit tenant: {} with role: {}", user.getUsername(), user.getPrimaryRole());
            return user;

        } catch (Exception e) {
            log.error("Error loading user: {} in tenant: {}", username, tenantId, e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
}