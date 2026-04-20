package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for admin information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin response DTO")
public class AdminResponse {

    @Schema(description = "Admin ID")
    private UUID id;

    @Schema(description = "Associated User ID")
    private UUID userId;

    @Schema(description = "Employee ID")
    private String employeeId;

    // Personal Information
    @Schema(description = "Admin's first name")
    private String firstName;

    @Schema(description = "Admin's last name")
    private String lastName;

    @Schema(description = "Admin's full name")
    private String fullName;

    @Schema(description = "Admin's date of birth")
    private LocalDate dateOfBirth;

    @Schema(description = "Admin's age")
    private Integer age;

    @Schema(description = "Admin's gender")
    private String gender;

    // Contact Information
    @Schema(description = "Admin's email (from user)")
    private String email;

    @Schema(description = "Admin's phone number")
    private String phone;

    @Schema(description = "Alternate contact number")
    private String alternatePhone;

    @Schema(description = "Admin's current address")
    private String address;

    @Schema(description = "City of residence")
    private String city;

    @Schema(description = "State of residence")
    private String state;

    @Schema(description = "Postal/ZIP code")
    private String postalCode;

    @Schema(description = "Country of residence")
    private String country;

    // Professional Information
    @Schema(description = "Department")
    private String department;

    @Schema(description = "Job title/position")
    private String designation;

    @Schema(description = "Date when admin joined")
    private LocalDate joinDate;

    @Schema(description = "Years of service")
    private Integer yearsOfService;

    @Schema(description = "Admin's salary")
    private BigDecimal salary;

    @Schema(description = "Employment type")
    private String employmentType;

    // Status and Permissions
    @Schema(description = "Admin status")
    private String status;

    @Schema(description = "Set of permissions")
    private Set<String> permissions;

    @Schema(description = "Profile image URL")
    private String profileImageUrl;

    @Schema(description = "Additional notes")
    private String notes;

    // Audit Information
    @Schema(description = "Tenant ID")
    private String tenantId;

    @Schema(description = "Creation date")
    private LocalDate createdAt;

    @Schema(description = "Last updated date")
    private LocalDate updatedAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated by")
    private String updatedBy;

    /**
     * Calculate age from date of birth
     */
    public Integer getAge() {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return null;
    }

    /**
     * Calculate years of service from join date
     */
    public Integer getYearsOfService() {
        if (joinDate != null) {
            return Period.between(joinDate, LocalDate.now()).getYears();
        }
        return null;
    }

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : (lastName != null ? lastName : "");
    }
}
