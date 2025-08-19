package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateUserRequest;
import com.schoolmgmt.dto.request.UpdateUserRequest;
import com.schoolmgmt.dto.response.UserResponse;
import com.schoolmgmt.dto.response.UserStatistics;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final EmailService emailService;

    /**
     * Get all users for current tenant
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(this::toUserResponse);
    }

    /**
     * Get users by role
     */
    public List<UserResponse> getUsersByRole(String role) {
        String tenantId = TenantContext.requireCurrentTenant();
        User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
        
        List<User> users = userRepository.findByPrimaryRoleAndTenantId(userRole, tenantId);
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
    public UserResponse createUser(CreateUserRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        // Check if username exists
        if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email exists
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Parse role
        User.UserRole role = User.UserRole.valueOf(request.getRole().toUpperCase());
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .primaryRole(role)
                .roles(Set.of(role))
                .status(request.isSendInvitation() ? User.UserStatus.PENDING : User.UserStatus.ACTIVE)
                .emailVerified(!request.isSendInvitation())
                .enabled(!request.isSendInvitation())
                .build();
        
        user.setTenantId(tenantId);
        User savedUser = userRepository.save(user);
        
        // Send invitation email if requested
        if (request.isSendInvitation()) {
            emailService.sendEmailVerification(savedUser);
        }
        
        log.info("User created: {} in tenant: {}", savedUser.getEmail(), tenantId);
        
        return toUserResponse(savedUser);
    }

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
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsersByRoleAndTenant(null, tenantId);
        long teachers = userRepository.countActiveUsersByRoleAndTenant(User.UserRole.TEACHER, tenantId);
        long students = userRepository.countActiveUsersByRoleAndTenant(User.UserRole.STUDENT, tenantId);
        long parents = userRepository.countActiveUsersByRoleAndTenant(User.UserRole.PARENT, tenantId);
        
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
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .primaryRole(user.getPrimaryRole().name())
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
}
