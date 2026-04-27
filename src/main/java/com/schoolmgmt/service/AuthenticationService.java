package com.schoolmgmt.service;

//import com.schoolmgmt.dto.ApiResponse;
import  com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.common.UserInfo;
import com.schoolmgmt.dto.request.*;
import com.schoolmgmt.dto.response.AuthResponse;
import com.schoolmgmt.exception.PasswordChangeException;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.security.JwtService;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.schoolmgmt.model.Tenant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for handling authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;
    private final TenantRepository tenantRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthResponse authenticate(LoginRequest request, String tenantId) {
        log.info("Login attempt - Email: {}, Phone: {}, Tenant: {}",
            request.getEmail() != null ? maskEmail(request.getEmail()) : "not provided",
            request.getPhone() != null ? maskPhone(request.getPhone()) : "not provided",
            tenantId != null ? tenantId : "will be auto-detected");

        // Validate that at least one identifier is provided
        if (!request.isValid()) {
            log.warn("Login failed: Neither email nor phone provided");
            throw new IllegalArgumentException("Either email or phone number must be provided");
        }

        try {
            Optional<User> userOptional = Optional.empty();
            String finalTenantId = tenantId;

            // Auto-detect tenantId if not provided
            if (tenantId == null) {
                log.debug("Tenant ID not provided, attempting to auto-detect from email/phone");

                // Try to find user by email globally
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    log.debug("Searching for user by email globally: {}", maskEmail(request.getEmail()));
                    userOptional = userRepository.findByEmail(request.getEmail());
                }

                // If not found by email, try phone globally
                if (userOptional.isEmpty() && request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                    log.debug("Searching for user by phone globally: {}", maskPhone(request.getPhone()));
                    userOptional = userRepository.findByPhone(request.getPhone());
                }

                // If user found, use their tenantId
                if (userOptional.isPresent()) {
                    finalTenantId = userOptional.get().getTenantId();
                    log.info("Auto-detected tenant ID: {} from user credentials", finalTenantId);
                } else {
                    log.warn("User not found globally with provided credentials");
                    throw new UsernameNotFoundException("Invalid credentials");
                }
            } else {
                // Find user by email and tenant ID if email is provided (priority)
                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    log.debug("Searching for user by email: {} in tenant: {}", maskEmail(request.getEmail()), finalTenantId);
                    userOptional = userRepository.findByEmailAndTenantId(request.getEmail(), finalTenantId);
                }
                // Find user by phone and tenant ID if phone is provided and email not found or not provided
                if (userOptional.isEmpty() && request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                    log.debug("Searching for user by phone: {} in tenant: {}", maskPhone(request.getPhone()), finalTenantId);
                    userOptional = userRepository.findByPhoneAndTenantId(request.getPhone(), finalTenantId);
                }
            }

            // Find user in the specified tenant
            if (userOptional.isEmpty()) {
                log.warn("User not found for provided credentials in tenant: {}", finalTenantId);
                throw new UsernameNotFoundException("Invalid credentials");
            }
            User user = userOptional.get();


            log.info("User found for login - User ID: {}, Email: {}, Phone: {}, Tenant: {}",
                user.getId(), maskEmail(user.getEmail()), maskPhone(user.getPhone()), finalTenantId);

            // Set tenant context based on user's tenant
            TenantContext.setCurrentTenant(user.getTenantId());




            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
                    log.warn("Account locked for user: {} (ID: {})", maskEmail(user.getEmail()), user.getId());
                    throw new BadCredentialsException("Account is locked. Please try again later.");
                } else {
                    // Unlock if lock period has expired
                    userRepository.unlockUserAccount(user.getId());
                    user.setAccountNonLocked(Boolean.TRUE);
                    user.setFailedLoginAttempts(Integer.valueOf(0));
                    log.info("Account unlocked for user: {} (ID: {})", maskEmail(user.getEmail()), user.getId());
                }
            }

            // Check if email is verified
            if (!user.isEmailVerified()) {
                log.warn("Email not verified for user: {} (ID: {})", maskEmail(user.getEmail()), user.getId());
                throw new BadCredentialsException("Email not verified. Please check your email for verification link.");
            }

            // Authenticate using the user's actual username (required by Spring Security)
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(), // Use actual username from user record
                                request.getPassword()
                        )
                );
                log.info("Authentication successful for user: {} (ID: {})", maskEmail(user.getEmail()), user.getId());
            } catch (BadCredentialsException e) {
                // Increment failed login attempts
                handleFailedLogin(user);
                log.warn("Invalid password for user: {} (ID: {})", maskEmail(user.getEmail()), user.getId());
                throw new BadCredentialsException("Invalid credentials");
            }


            // ✅ If user is still using system-provided password
            if (user.isTemporaryPassword()) {
                // Return response without tokens, but with reset flag
                return AuthResponse.builder()
                        .user(UserInfo.builder()
                                .id(user.getId().toString())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .role(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : null)
                                .tenantId(user.getTenantId())
                                .emailVerified(user.isEmailVerified())
                                .mfaEnabled(user.isMfaEnabled())
                                .build())
                        .passwordResetRequired(true) // 🔑 frontend can redirect to reset-password
                        .build();
            }

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                userRepository.resetFailedLoginAttempts(user.getId());
            }

            // Update last login
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

            // Get student login required flag from tenant
            Optional<Tenant> tenant = tenantRepository.findByIdentifier(user.getTenantId());
            Boolean studentLoginRequired = tenant.isPresent() ? tenant.get().getStudentLoginRequired() : Boolean.FALSE;

            // Generate tokens with all roles
            java.util.Set<String> allRoles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());
            String accessToken = jwtService.generateTokenWithAllRoles(user, user.getTenantId(), allRoles, user.getUsername(), studentLoginRequired);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Build response
            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : null)
                    .tenantId(user.getTenantId())
                    .emailVerified(user.isEmailVerified())
                    .mfaEnabled(user.isMfaEnabled())
                    .build();

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // Convert to seconds
                    .user(userInfo)
                    .passwordResetRequired(false)
                    .build();

        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Register new user
     */
    public ApiResponse register(RegisterRequest request, String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);

            log.info("Registration attempt - Email: {}, Phone: {}, Username: {}",
                maskEmail(request.getEmail()), maskPhone(request.getPhone()), request.getUsername());

            // Check if username exists
            if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
                log.warn("Registration failed: Username already exists - {}", request.getUsername());
                throw new IllegalArgumentException("Username already exists");
            }

            // Check if email exists
            if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                log.warn("Registration failed: Email already exists - {}", maskEmail(request.getEmail()));
                throw new IllegalArgumentException("Email already exists");
            }

            // Check if phone exists
            if (userRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
                log.warn("Registration failed: Phone number already exists - {}", maskPhone(request.getPhone()));
                throw new IllegalArgumentException("Phone number already exists");
            }

            // Parse role
            User.UserRole role;
            try {
                role = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }

            // Create user
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .roles(Set.of(role))
                    .status(User.UserStatus.PENDING)
                    .emailVerified(Boolean.FALSE)
                    .isActive(Boolean.FALSE)
                    .emailVerificationToken(generateToken())
                    .build();

            user.setTenantId(tenantId);
            User savedUser = userRepository.save(user);

            // Send verification email
            emailService.sendEmailVerification(savedUser);

            log.info("User registered successfully: {} in tenant: {}", savedUser.getEmail(), tenantId);

            return ApiResponse.success(
                    "User registered successfully. Please check your email for verification.",
                    Map.of("id", savedUser.getId().toString())
            );

        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtService.validateToken(refreshToken)) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            String username = jwtService.extractUsername(refreshToken);
            String tenantId = jwtService.extractTenantId(refreshToken);

            TenantContext.setCurrentTenant(tenantId);

            User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Get student login required flag from tenant
            Optional<Tenant> tenant = tenantRepository.findByIdentifier(tenantId);
            Boolean studentLoginRequired = tenant.isPresent() ? tenant.get().getStudentLoginRequired() : Boolean.FALSE;

            // Generate new access token with all roles
            java.util.Set<String> allRoles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());
            String newAccessToken = jwtService.generateTokenWithAllRoles(user, tenantId, allRoles, user.getUsername(), studentLoginRequired);

            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : null)
                    .tenantId(tenantId)
                    .emailVerified(user.isEmailVerified())
                    .mfaEnabled(user.isMfaEnabled())
                    .build();

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Return same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000)
                    .user(userInfo)
                    .build();

        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Logout user and blacklist token
     */
    public void logout(String token, String username) {
        // Extract token from Bearer prefix
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Add token to blacklist
        tokenBlacklistService.blacklistToken(token);

        log.info("User logged out: {}", username);
    }

    /**
     * Reset Password First Time
     */
    @Transactional
    public void firstTimePasswordChange(FirstTimePasswordChange request) {
        log.info("Password reset confirmation attempt for username {}", request.getUsername());


        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + request.getUsername()
                ));
        try {
            // Set current tenant for multi-tenant handling
            TenantContext.setCurrentTenant(user.getTenantId());


            // 1. Validate if user requires initial reset
            if (!user.isTemporaryPassword()) {
                throw new PasswordChangeException("Initial reset not required");
            }


            // 2. Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new PasswordChangeException("Invalid current password");

            }


            // 3. Encode new password
            String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
            // 4. Update password + flags in DB
            int updated = userRepository.setInitialPasswordReset(user.getId(), encodedNewPassword, LocalDateTime.now());
//            userRepository.updatingIsTemporaryPasswordValue(false);
            log.info("Password update result = {}", String.valueOf(updated));

            if (updated == 0) {
                throw new PasswordChangeException("Password update failed, no row updated!");
            }

            // Optionally, send confirmation email
            emailService.sendPasswordChangeConfirmation(user);


        } finally {
            // Always clear tenant context to prevent memory leaks
            TenantContext.clear();
        }


    }

    /**
     * Initiate password reset
     * Accepts either email or phone for user identification, but always sends reset email to user's registered email
     */
    public void initiatePasswordReset(PasswordResetRequest request, String tenantId) {
        log.info("Password reset request received - Email: {}, Phone: {}, Tenant: {}",
            request.getEmail() != null ? maskEmail(request.getEmail()) : "not provided",
            request.getPhone() != null ? maskPhone(request.getPhone()) : "not provided",
            tenantId);

        // Validate that at least one identifier is provided
        if (!request.isValid()) {
            log.warn("Password reset request failed: Neither email nor phone provided");
            throw new IllegalArgumentException("Either email or phone number must be provided");
        }

        Optional<User> userOptional = Optional.empty();

        // Find user by email and tenant ID if email is provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            log.debug("Searching for user by email: {} in tenant: {}", maskEmail(request.getEmail()), tenantId);
            userOptional = userRepository.findByEmailAndTenantId(request.getEmail(), tenantId);
        }
        // Find user by phone and tenant ID if phone is provided and email not found or not provided
        if (userOptional.isEmpty() && request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            log.debug("Searching for user by phone: {} in tenant: {}", maskPhone(request.getPhone()), tenantId);
            userOptional = userRepository.findByPhoneAndTenantId(request.getPhone(), tenantId);
        }

        // Process password reset if user found
        userOptional.ifPresentOrElse(user -> {
            try {
                TenantContext.setCurrentTenant(user.getTenantId());
                log.info("User found for password reset - User ID: {}, Email: {}, Phone: {}, Tenant: {}",
                    user.getId(), maskEmail(user.getEmail()), maskPhone(user.getPhone()), tenantId);

                String resetToken = generateToken();
                LocalDateTime expiry = LocalDateTime.now().plusHours(1);

                userRepository.setPasswordResetToken(user.getId(), resetToken, expiry);
                emailService.sendPasswordResetEmail(user, resetToken);

                log.info("Password reset initiated successfully for user: {} (ID: {}) in tenant: {}",
                    maskEmail(user.getEmail()), user.getId(), tenantId);
            } catch (Exception e) {
                log.error("Error initiating password reset for user: {} (ID: {}) in tenant: {}",
                    maskEmail(user.getEmail()), user.getId(), tenantId, e);
                throw new RuntimeException("Failed to initiate password reset", e);
            } finally {
                TenantContext.clear();
            }
        }, () -> {
            // User not found - log but don't reveal to prevent enumeration
            log.info("No user found for the provided credentials in tenant: {}. Password reset not initiated.", tenantId);
        });

        // Always return success to prevent email/phone enumeration
        log.debug("Password reset request processed successfully");
    }

    /**
     * Mask email for logging (show first 2 chars and domain)
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "not provided";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***@***";
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * Mask phone for logging (show first 2 and last 2 digits)
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }


    
    /**
     * Reset password with token
     */
    public void resetPassword(PasswordResetConfirmRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        
        if (!user.isPasswordResetTokenValid()) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        
        try {
            TenantContext.setCurrentTenant(user.getTenantId());
            
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            userRepository.updatePassword(user.getId(), encodedPassword, LocalDateTime.now());
            
            // Send confirmation email
            emailService.sendPasswordChangeConfirmation(user);
            
            log.info("Password reset successful for: {}", user.getEmail());
        } finally {
            TenantContext.clear();
        }
    }
    
    /**
     * Verify email with token
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        
        try {
            TenantContext.setCurrentTenant(user.getTenantId());
            
            userRepository.verifyEmail(user.getId());
            
            log.info("Email verified for: {}", user.getEmail());
        } finally {
            TenantContext.clear();
        }
    }
    
    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email, String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            
            userRepository.findByEmailAndTenantId(email, tenantId)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    String newToken = generateToken();
                    user.setEmailVerificationToken(newToken);
                    userRepository.save(user);
                    
                    emailService.sendEmailVerification(user);
                    log.info("Verification email resent to: {}", email);
                });
        } finally {
            TenantContext.clear();
        }
    }
    
    /**
     * Change password for authenticated user
     */
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsernameOrEmailAndTenantId(
                        username, TenantContext.getCurrentTenant())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
//        userRepository.updatePassword(user.getId(), encodedPassword, LocalDateTime.now());
        userRepository.updatePassword(user.getId(), encodedPassword, LocalDateTime.now());


        // Send confirmation email
        emailService.sendPasswordChangeConfirmation(user);
        
        log.info("Password changed for user: {}", username);
    }
    
    /**w
     * Handle failed login attempt
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        
        if (attempts >= maxLoginAttempts) {
            // Lock account
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
            userRepository.lockUserAccount(user.getId(), lockUntil);
            log.warn("Account locked due to {} failed login attempts: {}", attempts, user.getEmail());
        } else {
            // Increment failed attempts
            userRepository.incrementFailedLoginAttempts(user.getId());
        }
    }
    
    /**
     * Generate random token
     */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
