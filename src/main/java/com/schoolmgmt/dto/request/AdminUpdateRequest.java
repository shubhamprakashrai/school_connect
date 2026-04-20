package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Request DTO for updating admin information.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Schema(description = "Request DTO for updating admin information")
public class AdminUpdateRequest {

    // Personal Information
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Schema(description = "Admin's first name", example = "John")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Schema(description = "Admin's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Admin's date of birth", example = "1985-06-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Admin's gender", example = "MALE")
    private Admin.Gender gender;

    // Contact Information
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    @Schema(description = "Admin's phone number", example = "9876543210")
    private String phone;

    @Pattern(regexp = "^[0-9]{10}$", message = "Alternate phone number must be exactly 10 digits")
    @Schema(description = "Alternate contact number", example = "9876543211")
    private String alternatePhone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Admin's current address", example = "123 Main Street")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City of residence", example = "New York")
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

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    @Schema(description = "Job title/position of the admin", example = "Principal")
    private String designation;

    @Schema(description = "Admin's salary", example = "75000.00")
    private BigDecimal salary;

    @Schema(description = "Type of employment", example = "FULL_TIME")
    private Admin.EmploymentType employmentType;

    // Status and Permissions
    @Schema(description = "Admin status (ACTIVE, INACTIVE, SUSPENDED, TERMINATED)", example = "ACTIVE")
    private Admin.AdminStatus status;

    @Schema(description = "Set of permissions granted to the admin", example = "[USER_MANAGEMENT, REPORT_VIEW]")
    private Set<String> permissions;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    @Schema(description = "URL to admin's profile image", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes about the admin", example = "Experienced administrator")
    private String notes;
}
