package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Parent/Guardian entity representing parents or guardians in the school system.
 */
@Entity
@Table(name = "parents",
       indexes = {
           @Index(name = "idx_parent_email", columnList = "email, tenant_id", unique = true),
           @Index(name = "idx_parent_phone", columnList = "phone, tenant_id"),
           @Index(name = "idx_parent_status", columnList = "status"),
           @Index(name = "idx_parent_type", columnList = "parent_type")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user", "children", "wards"})
public class Parent extends BaseEntity {

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_type", nullable = false, length = 20)
    private ParentType parentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Contact Information
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "work_phone", length = 20)
    private String workPhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Professional Information
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "employer", length = 200)
    private String employer;

    @Column(name = "work_address", columnDefinition = "TEXT")
    private String workAddress;

    @Column(name = "annual_income", length = 50)
    private String annualIncome;

    @Column(name = "education_level", length = 100)
    private String educationLevel;

    // Identification
    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "voter_id", length = 20)
    private String voterId;

    // Relationship Information
    @Column(name = "relationship_to_student", length = 50)
    private String relationshipToStudent; // Father, Mother, Guardian, etc.

    @Column(name = "is_primary_contact")
    @Builder.Default
    private Boolean isPrimaryContact = false;

    @Column(name = "is_emergency_contact")
    @Builder.Default
    private Boolean isEmergencyContact = false;

    @Column(name = "can_pickup_child")
    @Builder.Default
    private Boolean canPickupChild = true;

    // Communication Preferences
    @Column(name = "preferred_language", length = 50)
    @Builder.Default
    private String preferredLanguage = "English";

    @Column(name = "receive_sms")
    @Builder.Default
    private Boolean receiveSms = true;

    @Column(name = "receive_email")
    @Builder.Default
    private Boolean receiveEmail = true;

    @Column(name = "receive_app_notifications")
    @Builder.Default
    private Boolean receiveAppNotifications = true;

    // Portal Access
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ParentStatus status = ParentStatus.ACTIVE;

    @Column(name = "portal_access_enabled")
    @Builder.Default
    private Boolean portalAccessEnabled = true;

    @Column(name = "last_portal_login")
    private LocalDateTime lastPortalLogin;

    // Documents
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "id_proof_url", length = 500)
    private String idProofUrl;

    @Column(name = "documents", columnDefinition = "TEXT")
    private String documents; // JSON array of document URLs

    // System User Link
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user; // Link to user account for portal login

    // Relationships
    @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Student> children = new HashSet<>(); // Biological children

    @ManyToMany(mappedBy = "guardians", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Student> wards = new HashSet<>(); // Guardian relationship

    // Notes and Remarks
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    // Business Methods
    public String getFullName() {
        StringBuilder name = new StringBuilder(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            name.append(" ").append(middleName);
        }
        name.append(" ").append(lastName);
        return name.toString();
    }

    public boolean isActive() {
        return status == ParentStatus.ACTIVE;
    }

    public Set<Student> getAllStudents() {
        Set<Student> allStudents = new HashSet<>();
        allStudents.addAll(children);
        allStudents.addAll(wards);
        return allStudents;
    }

    public boolean hasAccessToStudent(String studentId) {
        return getAllStudents().stream()
                .anyMatch(s -> s.getId().toString().equals(studentId));
    }

    // Enums
    public enum ParentType {
        FATHER,
        MOTHER,
        GUARDIAN,
        GRANDFATHER,
        GRANDMOTHER,
        UNCLE,
        AUNT,
        BROTHER,
        SISTER,
        OTHER
    }

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum ParentStatus {
        ACTIVE,     // Active parent/guardian
        INACTIVE,   // Temporarily inactive
        BLOCKED,    // Blocked from portal access
        DELETED     // Soft deleted
    }
}
