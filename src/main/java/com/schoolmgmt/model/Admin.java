package com.schoolmgmt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Admin entity representing an administrative staff member in the school system.
 * This entity extends the User authentication system with admin-specific profile information.
 */
@Entity
@Table(name = "admins",
       indexes = {
           @Index(name = "idx_admin_user_id", columnList = "user_id", unique = true),
           @Index(name = "idx_admin_employee_id", columnList = "employee_id, tenant_id", unique = true),
           @Index(name = "idx_admin_status", columnList = "status"),
           @Index(name = "idx_admin_department", columnList = "department")
       })
@Where(clause = "is_deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user"})
public class Admin extends BaseEntity {

    @Column(name = "employee_id", nullable = false, length = 50)
    @Schema(description = "Unique employee ID for the admin")
    private String employeeId;

    // Link to User entity for authentication
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Schema(description = "Associated user account for authentication")
    private User user;

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    @Schema(description = "Admin's first name")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @Schema(description = "Admin's last name")
    private String lastName;

    @Column(name = "date_of_birth")
    @Schema(description = "Admin's date of birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    @Schema(description = "Admin's gender")
    private Gender gender;

    // Contact Information
    @Column(name = "phone", nullable = false, length = 20)
    @Schema(description = "Admin's phone number")
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    @Schema(description = "Alternate contact number")
    private String alternatePhone;

    @Column(name = "address", columnDefinition = "TEXT")
    @Schema(description = "Admin's current address")
    private String address;

    @Column(name = "city", length = 100)
    @Schema(description = "City of residence")
    private String city;

    @Column(name = "state", length = 100)
    @Schema(description = "State of residence")
    private String state;

    @Column(name = "postal_code", length = 20)
    @Schema(description = "Postal/ZIP code")
    private String postalCode;

    @Column(name = "country", length = 100)
    @Schema(description = "Country of residence")
    private String country;

    // Professional Information
    @Column(name = "department", length = 100)
    @Schema(description = "Department the admin belongs to")
    private String department;

    @Column(name = "designation", nullable = false, length = 100)
    @Schema(description = "Job title/position of the admin")
    private String designation;

    @Column(name = "join_date", nullable = false)
    @Schema(description = "Date when admin joined the organization")
    private LocalDate joinDate;

    @Column(name = "salary", precision = 10, scale = 2)
    @Schema(description = "Admin's salary")
    private java.math.BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    @Schema(description = "Type of employment (FULL_TIME, PART_TIME, CONTRACT)")
    private EmploymentType employmentType;

    // Status and Permissions
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    @Schema(description = "Admin status (ACTIVE, INACTIVE, SUSPENDED, TERMINATED)")
    private AdminStatus status = AdminStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission", length = 100)
    @Schema(description = "Set of permissions granted to the admin")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "profile_image_url", length = 500)
    @Schema(description = "URL to admin's profile image")
    private String profileImageUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Schema(description = "Additional notes about the admin")
    private String notes;

    /**
     * Employment type enum
     */
    public enum EmploymentType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        INTERN
    }

    /**
     * Admin status enum
     */
    public enum AdminStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        TERMINATED
    }

    /**
     * Gender enum
     */
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }
}
