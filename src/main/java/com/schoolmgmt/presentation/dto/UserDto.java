package com.schoolmgmt.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User related DTOs
 */
public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User response")
    public static class UserResponse {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private String avatarUrl;
        private String primaryRole;
        private Set<String> roles;
        private String status;
        private boolean emailVerified;
        private boolean mfaEnabled;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create user request")
    public static class CreateUserRequest {
        
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        @Schema(description = "Username", example = "john_doe")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "john.doe@school.com")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$")
        @Schema(description = "Password", example = "SecurePass123!")
        private String password;
        
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        @Schema(description = "First name", example = "John")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        @Schema(description = "Last name", example = "Doe")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @NotBlank(message = "Role is required")
        @Schema(description = "User role", example = "TEACHER")
        private String role;
        
        @Schema(description = "Send invitation email", example = "true")
        private boolean sendInvitation = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update user request")
    public static class UpdateUserRequest {
        
        @Size(max = 100)
        @Schema(description = "First name", example = "John")
        private String firstName;
        
        @Size(max = 100)
        @Schema(description = "Last name", example = "Doe")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @URL
        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User filter request")
    public static class UserFilterRequest {
        
        @Schema(description = "Filter by role", example = "TEACHER")
        private String role;
        
        @Schema(description = "Filter by status", example = "ACTIVE")
        private String status;
        
        @Schema(description = "Search by name or email", example = "john")
        private String search;
        
        @Schema(description = "Filter by email verified", example = "true")
        private Boolean emailVerified;
        
        @Schema(description = "Filter by MFA enabled", example = "false")
        private Boolean mfaEnabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User statistics")
    public static class UserStatistics {
        
        @Schema(description = "Total users", example = "150")
        private long totalUsers;
        
        @Schema(description = "Active users", example = "140")
        private long activeUsers;
        
        @Schema(description = "Total teachers", example = "20")
        private long teachers;
        
        @Schema(description = "Total students", example = "100")
        private long students;
        
        @Schema(description = "Total parents", example = "20")
        private long parents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Assign role request")
    public static class AssignRoleRequest {
        
        @NotBlank(message = "Role is required")
        @Schema(description = "Role to assign", example = "TEACHER")
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Reset password request")
    public static class AdminResetPasswordRequest {
        
        @NotBlank(message = "New password is required")
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$")
        @Schema(description = "New password", example = "NewSecurePass123!")
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update status request")
    public static class UpdateStatusRequest {
        
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(PENDING|ACTIVE|INACTIVE|SUSPENDED|DELETED)$")
        @Schema(description = "User status", example = "ACTIVE")
        private String status;
    }
}
