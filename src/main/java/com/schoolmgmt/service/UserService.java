package com.schoolmgmt.service;

import com.schoolmgmt.dto.common.UserRequest;
import com.schoolmgmt.dto.request.CreateUserRequest;
import com.schoolmgmt.dto.request.UpdateUserRequest;
import com.schoolmgmt.dto.response.UserResponse;
import com.schoolmgmt.dto.response.UserStatistics;
import com.schoolmgmt.exception.EmailSendException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.security.JwtService;
import com.schoolmgmt.util.TenantContext;
import com.schoolmgmt.util.TenantTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import  com.schoolmgmt.dto.common.UserRequest;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Tenant;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.UserIdGeneratorBasedonTenantIdentifies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for user management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TenantTokenUtil tenantTokenUtil;

    /**
     * Generic user creation for any role (ADMIN, TEACHER, STUDENT, PARENT, STAFF)
     *
     * @param role             Role name (for backward compatibility)
     * @param request          UserRequest DTO
     * @param tenantIdentifier Tenant identifier
     * @return Saved User
     */
    public User createUser(String role, @Valid UserRequest request, String tenantIdentifier) {
        Set<User.UserRole> roles = new HashSet<>();
        try {
            roles.add(User.UserRole.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", role);
            throw new BusinessException("Invalid role: " + role);
        }
        return createUserWithRoles(roles, request, tenantIdentifier);
    }

    /**
     * User creation with multiple roles support
     *
     * @param roles            Set of roles for the user
     * @param request          UserRequest DTO
     * @param tenantIdentifier Tenant identifier
     * @return Saved User
     */
    public User createUserWithRoles(Set<User.UserRole> roles, @Valid UserRequest request, String tenantIdentifier) {
        try {
            log.info("Starting user creation for tenant {} with roles {}", tenantIdentifier, roles);

            // --- Manual validation for phone format (10 digits) ---
            if (request.getPhone() != null) {
                String cleanPhone = request.getPhone().trim();
                if (!cleanPhone.matches("^[0-9]{10}$")) {
                    log.warn("Invalid phone format for user creation: {}", cleanPhone);
                    throw new BusinessException("Phone number must be exactly 10 digits (no spaces or special characters): " + cleanPhone);
                }
                if (cleanPhone.length() != 10) {
                    log.warn("Invalid phone length for user creation: {} digits", cleanPhone.length());
                    throw new BusinessException("Phone number must be exactly 10 digits, provided: " + cleanPhone.length() + " digits");
                }
            }

            // --- Validate duplicates ---
            // For ADMIN, check globally across all tenants
            if (roles.contains(User.UserRole.ADMIN)) {
                log.info("ADMIN role detected, performing global duplicate checks");
                if (userRepository.existsByEmail(request.getEmail())) {
                    log.warn("Duplicate email detected globally for ADMIN: {}", request.getEmail());
                    throw new BusinessException("Email already registered in the system: " + request.getEmail());
                }
                if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                    log.warn("Duplicate phone detected globally for ADMIN: {}", request.getPhone());
                    throw new BusinessException("Phone number already exists in the system: " + request.getPhone());
                }
            }

            // Check for existing user with same email within the same tenant
            if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantIdentifier)) {
                log.warn("Duplicate email detected in tenant {}: {} with roles {}", tenantIdentifier, request.getEmail(), roles);
                throw new BusinessException("Email already registered in this school: " + request.getEmail());
            }

            if (request.getPhone() != null && userRepository.existsByPhoneAndTenantId(request.getPhone(), tenantIdentifier)) {
                log.warn("Duplicate phone detected in tenant {}: {}", tenantIdentifier, request.getPhone());
                throw new BusinessException("Phone number already exists in this school: " + request.getPhone());
            }

            // --- Generate userId ---
            Integer lastSequence = Optional.ofNullable(userRepository.findMaxSequenceForTenant(tenantIdentifier)).orElse(0);
            String userId = UserIdGeneratorBasedonTenantIdentifies.generateNextCode(tenantIdentifier, lastSequence, 5);
            log.info("Generated userId {} for tenant {}", userId, tenantIdentifier);

            // --- Validate roles ---
            if (roles == null || roles.isEmpty()) {
                log.error("No roles provided for user creation");
                throw new BusinessException("At least one role must be provided");
            }

            // --- Temporary password ---
            String tempPassword = generateTempPassword();
            log.info("Temporary password generated for user {}", userId);

            // --- Build user object ---
            User user = User.builder()
                    .userId(userId)
                    .username(userId)
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(tempPassword))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .roles(roles)
                    .status(User.UserStatus.ACTIVE)
                    .emailVerified(true)
                    .isActive(true)
                    .temporaryPassword(true) // important for first-time login
                    .tempPasswordForFirstTime(tempPassword)
                    .avatarUrl(request.getAvatarUrl())
                    .referenceId(request.getReferenceId())
                    .referenceType(request.getReferenceType())
                    .build();

//            String Role = TenantContext.get;
//            if (tenantId == null) {
//                log.error("Tenant ID not found in context while creating teacher");
//                throw new IllegalStateException("Cannot create teacher without tenant context");
//            }


            user.setTenantId(tenantIdentifier);


            //fetching value of role and username from the token
            String tenantrole = tenantTokenUtil.extractRoleFromCurrentToken();
            String username = tenantTokenUtil.extractUsernameFromCurrentToken();
            if(username==null && tenantrole==null)
            {
                user.setCreatedBy("System");
            }
            else
            {
                user.setCreatedBy(username + "-" + tenantrole);
            }


            // --- Save user ---
            User savedUser = userRepository.save(user);
            log.info("User created successfully: {} (roles: {}) in tenant {}", savedUser.getUsername(), roles, tenantIdentifier);


            //
            // Extract from studentLoginRequired value from the token

            boolean isStudent = roles.contains(User.UserRole.STUDENT);
            Boolean studentLoginRequired = tenantTokenUtil.extractStudentLoginRequiredFromCurrentToken();

            log.debug("Student login validation - isStudent: {}, studentLoginRequired: {}", isStudent, studentLoginRequired);

            if (isStudent && studentLoginRequired != null && studentLoginRequired) {
                // Student and login required - call createuser service
                log.info("Student login required for tenant: {}, calling createuser service for user: {}",
                        tenantTokenUtil.extractTenantIdFromCurrentToken(), savedUser.getEmail());

                // TODO: Call your createuser service here
                // yourCreateUserService.createUserForStudent(savedUser);

                // Send welcome email for login-required students
                try {
                    sendWelcomeEmail(savedUser);
                    log.info("Welcome email sent successfully to: {}", savedUser.getEmail());
                } catch (EmailSendException e) {
                    log.error("Failed to send welcome email to: {}", savedUser.getEmail(), e);
                    throw new BusinessException("Failed to send welcome email", e);
                }

            } else if (isStudent && (studentLoginRequired == null || !studentLoginRequired)) {
                // Student and login not required - DO NOT SEND EMAIL
                log.info("Student login not required for user: {} - SKIPPING welcome email", savedUser.getEmail());
                // No email sent here - just skip

            } else {
                // Non-student users - send welcome email
                log.info("Non-student user: {} - sending welcome email", savedUser.getEmail());
                try {
                    sendWelcomeEmail(savedUser);
                    log.info("Welcome email sent successfully to: {}", savedUser.getEmail());
                } catch (EmailSendException e) {
                    log.error("Failed to send welcome email to: {}", savedUser.getEmail(), e);
                    throw new BusinessException("Failed to send welcome email", e);
                }
            }


            return savedUser;

        } catch (BusinessException e) {
            // Already handled expected errors
            throw e;
        } catch (Exception e) {
            // Unexpected errors
            log.error("Failed to create user for tenant {} with roles {}: {}", tenantIdentifier, roles, e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Internal server error while creating user: " + e.getMessage(), e);
        }
    }

    private String generateTempPassword() {
        // Example: 8 random chars + uppercase + digit + special
        return RandomStringUtils.randomAlphanumeric(8) + "A1!";
    }

    private void sendWelcomeEmail(User user) {
        Tenant tenant = tenantRepository.findByIdentifier(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", user.getTenantId()));

        String subject = "Welcome to School Management System - Your Account is Ready!";
        String loginUrl = "http://frontend-url/login?tenant=" + tenant.getSubdomain();

        String emailContent = String.format(
                "Dear %s,\n\n" +
                        "Your account has been created in '%s'.\n\n" +
                        "Login Details:\n" +
                        "URL: %s\n" +
                        "Username: %s\n" +
                        "User ID: %s\n" +
                        "Password: %s\n" +
                        "Tenant: %s\n\n" +
                        "Please login and reset your password on first login.\n\n" +
                        "Best regards,\nSchool Management System Team",
                user.getFullName(),
                tenant.getName(),
                loginUrl,
                user.getUsername(),
                user.getUserId(),
                user.getTempPasswordForFirstTime(),
                tenant.getSubdomain()
        );

        try {
            emailService.sendSimpleEmail(user.getEmail(), subject, emailContent);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage(), e);
            // Optionally notify admin or queue for retry
        }
    }


    /**
     * Get all users for current tenant
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(this::toUserResponse);
    }

    /**
     * Get users by role (supports multi-role system)
     */
    public List<UserResponse> getUsersByRole(String role) {
        String tenantId = TenantContext.requireCurrentTenant();
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        
        // Find users who have the specified role in their roles set
        List<User> users = userRepository.findByTenantId(tenantId).stream()
                .filter(user -> user.hasRole(userRole))
                .collect(Collectors.toList());
        
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        return toUserResponse(user);
    }

    /**
     * Create new user
     */
//    public UserResponse createUser(CreateUserRequest request) {
//        String tenantId = TenantContext.requireCurrentTenant();
//
//        // Check if username exists
//        if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
//            throw new IllegalArgumentException("Username already exists");
//        }
//
//        // Check if email exists
//        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
//            throw new IllegalArgumentException("Email already exists");
//        }
//
//        // Parse role
//        User.UserRole role = User.UserRole.valueOf(request.getRole().toUpperCase());
//
//        // Create user
//        User user = User.builder()
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .phone(request.getPhone())
//                .role(role)
//                .status(request.isSendInvitation() ? User.UserStatus.PENDING : User.UserStatus.ACTIVE)
//                .emailVerified(!request.isSendInvitation())
//                .isActive(!request.isSendInvitation())
//                .build();
//
//        user.setTenantId(tenantId);
//        User savedUser = userRepository.save(user);
//
//        // Send invitation email if requested
//        if (request.isSendInvitation()) {
//            emailService.sendEmailVerification(savedUser);
//        }
//
//        log.info("User created: {} in tenant: {}", savedUser.getEmail(), tenantId);
//
//        return toUserResponse(savedUser);
//    }

    /**
     * Update user
     */
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        
        return toUserResponse(updatedUser);
    }

    /**
     * Update user status
     */
    public void updateUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
        user.setStatus(userStatus);
        
        if (userStatus == User.UserStatus.ACTIVE) {
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }
        
        userRepository.save(user);
        log.info("User status updated: {} to {}", user.getEmail(), status);
    }

    /**
     * Delete user (soft delete)
     */
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        user.setStatus(User.UserStatus.DELETED);
        user.setEnabled(false);
        user.softDelete(TenantContext.getCurrentTenant());
        
        userRepository.save(user);
        log.info("User soft deleted: {}", user.getEmail());
    }

    /**
     * Assign role to user
     */
    public void assignRole(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        user.getRoles().add(userRole);
        
        userRepository.save(user);
        log.info("Role {} assigned to user: {}", role, user.getEmail());
    }

    /**
     * Remove role from user
     */
    public void removeRole(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        user.getRoles().remove(userRole);
        
        userRepository.save(user);
        log.info("Role {} removed from user: {}", role, user.getEmail());
    }

    /**
     * Reset user password (admin action)
     */
    public void resetUserPassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, encodedPassword, LocalDateTime.now());
        
        // Send notification email
        emailService.sendPasswordChangeConfirmation(user);
        
        log.info("Password reset for user: {}", user.getEmail());
    }

    /**
     * Unlock user account
     */
    public void unlockUserAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify tenant access
        if (!user.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        userRepository.unlockUserAccount(userId);
        log.info("Account unlocked for user: {}", user.getEmail());
    }

    /**
     * Get user statistics for tenant
     */
    public UserStatistics getUserStatistics() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        List<User> allUsers = userRepository.findByTenantId(tenantId);
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == User.UserStatus.ACTIVE).count();
        long teachers = allUsers.stream().filter(u -> u.hasRole(User.UserRole.TEACHER) && u.getStatus() == User.UserStatus.ACTIVE).count();
        long students = allUsers.stream().filter(u -> u.hasRole(User.UserRole.STUDENT) && u.getStatus() == User.UserStatus.ACTIVE).count();
        long parents = allUsers.stream().filter(u -> u.hasRole(User.UserRole.PARENT) && u.getStatus() == User.UserStatus.ACTIVE).count();
        
        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .teachers(teachers)
                .students(students)
                .parents(parents)
                .build();
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .primaryRole(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : null)
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .mfaEnabled(user.isMfaEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Manually verify user email (for testing/admin purposes)
     */
    public void manuallyVerifyEmail(UUID userId) {
        log.info("Manually verifying email for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Use the existing repository method to verify email
        userRepository.verifyEmail(userId);
        
        log.info("Email verified successfully for user: {} ({})", userId, user.getEmail());
    }
}
