package com.schoolmgmt.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tenant related DTOs
 */
public class TenantDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant registration request")
    public static class TenantRegistrationRequest {
        
        @NotBlank(message = "School name is required")
        @Size(min = 3, max = 200)
        @Schema(description = "Name of the school", example = "Greenwood International School")
        private String name;
        
        @NotBlank(message = "Subdomain is required")
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain can only contain lowercase letters, numbers, and hyphens")
        @Schema(description = "Subdomain for school access", example = "greenwood")
        private String subdomain;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Primary contact email", example = "admin@greenwood.edu")
        private String email;
        
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
        @Schema(description = "Primary contact phone", example = "+1234567890")
        private String phone;
        
        @NotBlank(message = "Address is required")
        @Schema(description = "School address")
        private String address;
        
        @NotBlank(message = "City is required")
        @Schema(description = "City", example = "New York")
        private String city;
        
        @NotBlank(message = "State is required")
        @Schema(description = "State/Province", example = "NY")
        private String state;
        
        @NotBlank(message = "Country is required")
        @Schema(description = "Country", example = "USA")
        private String country;
        
        @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Invalid postal code")
        @Schema(description = "Postal/ZIP code", example = "10001")
        private String postalCode;
        
        @NotBlank(message = "Subscription plan is required")
        @Pattern(regexp = "^(TRIAL|BASIC|STANDARD|PREMIUM|ENTERPRISE)$")
        @Schema(description = "Subscription plan", example = "TRIAL")
        private String subscriptionPlan;
        
        @Schema(description = "School website", example = "https://www.greenwood.edu")
        private String website;
        
        @NotNull(message = "Admin user details are required")
        @Schema(description = "Admin user details for the school")
        private AdminUserRequest adminUser;
        
        @Schema(description = "Additional configuration settings")
        private Map<String, String> configuration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Admin user details for tenant registration")
    public static class AdminUserRequest {
        
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        @Schema(description = "Admin username", example = "admin")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Admin email", example = "admin@greenwood.edu")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
        @Schema(description = "Admin password", example = "Admin@123")
        private String password;
        
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        @Schema(description = "Admin first name", example = "John")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        @Schema(description = "Admin last name", example = "Doe")
        private String lastName;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Admin phone number", example = "+1234567890")
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant registration response")
    public static class TenantRegistrationResponse {
        
        @Schema(description = "Tenant details")
        private TenantInfo tenant;
        
        @Schema(description = "Admin user details")
        private AdminInfo admin;
        
        @Schema(description = "Success message")
        private String message;
        
        @Schema(description = "Access URL for the tenant")
        private String accessUrl;
        
        @Schema(description = "Next steps for setup")
        private String[] nextSteps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant information")
    public static class TenantInfo {
        private String id;
        private String identifier;
        private String name;
        private String subdomain;
        private String status;
        private String subscriptionPlan;
        private LocalDateTime activatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Admin information")
    public static class AdminInfo {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant response")
    public static class TenantResponse {
        private String id;
        private String identifier;
        private String name;
        private String subdomain;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String website;
        private String logoUrl;
        private String status;
        private String subscriptionPlan;
        private TenantLimits limits;
        private LocalDateTime createdAt;
        private LocalDateTime activatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant limits and usage")
    public static class TenantLimits {
        private Integer maxStudents;
        private Integer currentStudents;
        private Integer maxTeachers;
        private Integer currentTeachers;
        private Integer maxStorageGb;
        private Integer currentStorageMb;
        
        @Schema(description = "Percentage of student limit used")
        public int getStudentUsagePercentage() {
            return maxStudents > 0 ? (currentStudents * 100) / maxStudents : 0;
        }
        
        @Schema(description = "Percentage of teacher limit used")
        public int getTeacherUsagePercentage() {
            return maxTeachers > 0 ? (currentTeachers * 100) / maxTeachers : 0;
        }
        
        @Schema(description = "Percentage of storage limit used")
        public int getStorageUsagePercentage() {
            int maxMb = maxStorageGb * 1024;
            return maxMb > 0 ? (currentStorageMb * 100) / maxMb : 0;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update tenant request")
    public static class UpdateTenantRequest {
        
        @Size(min = 3, max = 200)
        @Schema(description = "School name")
        private String name;
        
        @Email(message = "Invalid email format")
        @Schema(description = "Contact email")
        private String email;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Contact phone")
        private String phone;
        
        @Schema(description = "Address")
        private String address;
        
        @Schema(description = "City")
        private String city;
        
        @Schema(description = "State")
        private String state;
        
        @Schema(description = "Country")
        private String country;
        
        @Schema(description = "Postal code")
        private String postalCode;
        
        @URL
        @Schema(description = "Website URL")
        private String website;
        
        @URL
        @Schema(description = "Logo URL")
        private String logoUrl;
        
        @Schema(description = "Configuration settings")
        private Map<String, String> configuration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant statistics")
    public static class TenantStatistics {
        private long totalStudents;
        private long totalTeachers;
        private long totalParents;
        private long activeUsers;
        private long totalClasses;
        private double attendancePercentage;
        private long storageUsedMb;
        private Map<String, Long> usersByRole;
        private Map<String, Long> studentsByClass;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tenant activation request")
    public static class TenantActivationRequest {
        
        @NotBlank(message = "Activation code is required")
        @Schema(description = "Activation code sent via email")
        private String activationCode;
    }
}
