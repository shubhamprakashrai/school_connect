package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.*;
import com.schoolmgmt.dto.response.*;
import com.schoolmgmt.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Get all users with a specific role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        log.info("Fetching users with role: {}", role);
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user details by user ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user in the system")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        UserResponse user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user account status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateStatusRequest request) {
        log.info("Updating status for user: {} to {}", userId, request.getStatus());
        userService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete a user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        log.info("Deleting user: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/{userId}/roles")
    @Operation(summary = "Assign role to user", description = "Assign a new role to user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        log.info("Assigning role {} to user: {}", request.getRole(), userId);
        userService.assignRole(userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully"));
    }

    @DeleteMapping("/{userId}/roles/{role}")
    @Operation(summary = "Remove role from user", description = "Remove a role from user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> removeRole(
            @PathVariable UUID userId,
            @PathVariable String role) {
        log.info("Removing role {} from user: {}", role, userId);
        userService.removeRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
    }

    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Reset user password", description = "Admin reset of user password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> resetUserPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        log.info("Resetting password for user: {}", userId);
        userService.resetUserPassword(userId, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock a locked user account")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> unlockAccount(@PathVariable UUID userId) {
        log.info("Unlocking account for user: {}", userId);
        userService.unlockUserAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked successfully"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get statistics about users in the tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserStatistics> getUserStatistics() {
        log.info("Fetching user statistics");
        UserStatistics stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
}
