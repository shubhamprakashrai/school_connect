package com.schoolmgmt.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication related DTOs
 */
public class AuthDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Login request")
    public static class LoginRequest {
        
        @NotBlank(message = "Username is required")
        @Schema(description = "Username or email", example = "admin@school.com")
        private String username;
        
        @NotBlank(message = "Password is required")
        @Schema(description = "User password", example = "SecurePass123!")
        private String password;
        
        @NotBlank(message = "Tenant ID is required")
        @Schema(description = "Tenant identifier", example = "public")
        private String tenantId;
        
        @Schema(description = "Remember me flag for extended session", example = "false")
        private boolean rememberMe;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Registration request")
    public static class RegisterRequest {
        
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscore")
        @Schema(description = "Username", example = "john_doe")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "john.doe@school.com")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
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
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @NotBlank(message = "Role is required")
        @Schema(description = "User role", example = "TEACHER")
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Authentication response")
    public static class AuthResponse {
        
        @Schema(description = "JWT access token")
        private String accessToken;
        
        @Schema(description = "Refresh token")
        private String refreshToken;
        
        @Schema(description = "Token type", example = "Bearer")
        private String tokenType = "Bearer";
        
        @Schema(description = "Token expiration time in seconds", example = "86400")
        private long expiresIn;
        
        @Schema(description = "User information")
        private UserInfo user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User information")
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String tenantId;
        private boolean emailVerified;
        private boolean mfaEnabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Refresh token request")
    public static class RefreshTokenRequest {
        
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "Refresh token")
        private String refreshToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Password reset request")
    public static class PasswordResetRequest {
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "john.doe@school.com")
        private String email;
        
        @NotBlank(message = "Tenant ID is required")
        @Schema(description = "Tenant identifier", example = "public")
        private String tenantId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Password reset confirmation")
    public static class PasswordResetConfirmRequest {
        
        @NotBlank(message = "Reset token is required")
        @Schema(description = "Password reset token")
        private String token;
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
        @Schema(description = "New password", example = "NewSecurePass123!")
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Change password request")
    public static class ChangePasswordRequest {
        
        @NotBlank(message = "Current password is required")
        @Schema(description = "Current password")
        private String currentPassword;
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
        @Schema(description = "New password", example = "NewSecurePass123!")
        private String newPassword;
    }
}
