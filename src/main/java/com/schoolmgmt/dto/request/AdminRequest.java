package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.UserRequest;
import com.schoolmgmt.model.Admin;
import com.schoolmgmt.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

/**
 * Request DTO for creating a new admin.
 * This includes both user authentication details (via UserRequest) and admin-specific profile information.
 * Follows the same pattern as TeacherCreationRequest.
 */
@Data
@Schema(description = "Request DTO for creating a new admin")
public class AdminRequest {

    @NotNull(message = "User details are required")
    @Valid
    @Schema(description = "User authentication details for the admin (contains firstName, lastName, email, phone)", required = true)
    private UserRequest userRequest;

    @Schema(description = "Additional roles for this admin (e.g., TEACHER for teacher-admin)", example = "[TEACHER]")
    private Set<User.UserRole> additionalRoles = new HashSet<>();

    // Admin-specific profile information
    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 50, message = "Employee ID must be between 3 and 50 characters")
    @Schema(description = "Unique employee ID for the admin", example = "ADM001", required = true)
    private String employeeId;

    // Personal Information (comes from UserRequest: firstName, lastName, email, phone)
    @Schema(description = "Admin's date of birth", example = "1985-06-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Admin's gender", example = "MALE")
    private Admin.Gender gender;

    // Contact Information
    @Pattern(regexp = "^[0-9]{10}$", message = "Alternate phone number must be exactly 10 digits")
    @Schema(description = "Alternate contact number", example = "9876543211")
    private String alternatePhone;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Admin's current address", example = "123 Main Street", required = true)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City of residence", example = "New York", required = true)
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Schema(description = "State of residence", example = "NY")
    private String state;

    @Pattern(regexp = "^[0-9]{6}$", message = "Postal code must be exactly 6 digits")
    @Schema(description = "Postal/ZIP code", example = "10001")
    private String postalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Country of residence", example = "USA")
    private String country;

    // Professional Information
    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Schema(description = "Department the admin belongs to", example = "Administration")
    private String department;

    @NotBlank(message = "Designation is required")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    @Schema(description = "Job title/position of the admin", example = "Principal", required = true)
    private String designation;

    @NotNull(message = "Join date is required")
    @Schema(description = "Date when admin joined the organization", example = "2020-01-15", required = true)
    private LocalDate joinDate;

    @Schema(description = "Admin's salary", example = "75000.00")
    private BigDecimal salary;

    @Schema(description = "Type of employment", example = "FULL_TIME")
    private Admin.EmploymentType employmentType;

    @Schema(description = "Set of permissions granted to the admin", example = "[USER_MANAGEMENT, REPORT_VIEW]")
    private Set<String> permissions = new HashSet<>();

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    @Schema(description = "URL to admin's profile image", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes about the admin", example = "Experienced administrator")
    private String notes;
}
