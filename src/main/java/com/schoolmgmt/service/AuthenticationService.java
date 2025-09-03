package com.schoolmgmt.service;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.common.UserInfo;
import com.schoolmgmt.dto.request.*;
import com.schoolmgmt.dto.response.AuthResponse;
import com.schoolmgmt.model.User;
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

import java.time.LocalDateTime;
import java.util.Map;
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
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${app.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${app.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthResponse authenticate(LoginRequest request) {
        try {
            // Find user across all tenants
            User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            
            // Set tenant context based on user's tenant
            TenantContext.setCurrentTenant(user.getTenantId());
            
            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
                    throw new BadCredentialsException("Account is locked. Please try again later.");
                } else {
                    // Unlock if lock period has expired
                    userRepository.unlockUserAccount(user.getId());
                    user.setAccountNonLocked(true);
                    user.setFailedLoginAttempts(0);
                }
            }
            
            // Check if email is verified
            if (!user.isEmailVerified()) {
                throw new BadCredentialsException("Email not verified. Please check your email for verification link.");
            }
            
            // Authenticate
            try {
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                    )
                );
            } catch (BadCredentialsException e) {
                // Increment failed login attempts
                handleFailedLogin(user);
                throw new BadCredentialsException("Invalid credentials");
            }
            
            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                userRepository.resetFailedLoginAttempts(user.getId());
            }
            
            // Update last login
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
            
            // Generate tokens
            String accessToken = jwtService.generateToken(user, user.getTenantId(), user.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Build response
            UserInfo userInfo = UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
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
            
            // Check if username exists
            if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
                throw new IllegalArgumentException("Username already exists");
            }
            
            // Check if email exists
            if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                throw new IllegalArgumentException("Email already exists");
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
                .role(role)
                .status(User.UserStatus.PENDING)
                .emailVerified(false)
                .isActive(false)
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
            
            // Generate new access token
            String newAccessToken = jwtService.generateToken(user, tenantId, user.getRole().name());
            
            UserInfo userInfo = UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
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
     * Initiate password reset
     */
    public void initiatePasswordReset(PasswordResetRequest request) {
        // Find user by email across all tenants
        userRepository.findByEmail(request.getEmail())
            .ifPresent(user -> {
                try {
                    TenantContext.setCurrentTenant(user.getTenantId());
                    
                    String resetToken = generateToken();
                    LocalDateTime expiry = LocalDateTime.now().plusHours(1);
                    
                    userRepository.setPasswordResetToken(user.getId(), resetToken, expiry);
                    emailService.sendPasswordResetEmail(user, resetToken);
                    
                    log.info("Password reset initiated for: {}", user.getEmail());
                } finally {
                    TenantContext.clear();
                }
            });
        // Don't reveal if email exists or not
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
        userRepository.updatePassword(user.getId(), encodedPassword, LocalDateTime.now());
        
        // Send confirmation email
        emailService.sendPasswordChangeConfirmation(user);
        
        log.info("Password changed for user: {}", username);
    }
    
    /**
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
