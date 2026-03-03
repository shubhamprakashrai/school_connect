package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.model.User;
import com.schoolmgmt.model.Parent;

/**
 * Student entity representing a student in the school system.
 */
@Entity
@Table(name = "students",
        indexes = {
                @Index(name = "idx_student_roll_class", columnList = "roll_number, current_class_id, tenant_id", unique = true),
                @Index(name = "idx_student_email", columnList = "email, tenant_id"),
                @Index(name = "idx_student_status", columnList = "status"),
                @Index(name = "idx_student_class", columnList = "current_class_id"),
                @Index(name = "idx_student_section", columnList = "current_section_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Student extends BaseEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "roll_number", nullable = false, length = 20)
    private String rollNumber; // Roll number within class

    // Personal Information
    @ToString.Include
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @ToString.Include
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @ToString.Include
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @ToString.Include
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "religion", length = 50)
    private String religion;

    @Column(name = "caste", length = 50)
    private String caste;

    @Column(name = "category", length = 20)
    private String category; // General, OBC, SC, ST, etc.

    // Contact Information
    @ToString.Include
    @Column(name = "email", length = 100)
    private String email;

    @ToString.Include
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

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

    @ManyToOne
    @JoinColumn(name = "schoolClass", nullable = false)
    @ToString.Include
    private SchoolClass schoolClass;

    @ManyToOne
    @JoinColumn(name = "section", nullable = false)
    @ToString.Include
    private Section section;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "admission_class", length = 50)
    private String admissionClass; // Class at the time of admission

    @Column(name = "previous_school", length = 200)
    private String previousSchool;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    // Health Information
    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    // Parent/Guardian Information
    @Column(name = "father_name", length = 200)
    private String fatherName;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "father_phone", length = 20)
    private String fatherPhone;

    @Column(name = "father_email", length = 100)
    private String fatherEmail;

    @Column(name = "mother_name", length = 200)
    private String motherName;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Column(name = "mother_phone", length = 20)
    private String motherPhone;

    @Column(name = "mother_email", length = 100)
    private String motherEmail;

    @Column(name = "guardian_name", length = 200)
    private String guardianName;

    @Column(name = "guardian_relation", length = 50)
    private String guardianRelation;

    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(name = "guardian_email", length = 100)
    private String guardianEmail;

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    @Column(name = "emergency_contact_phone", nullable = false, length = 20)
    private String emergencyContactPhone;

    // Documents
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "birth_certificate_url", length = 500)
    private String birthCertificateUrl;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "documents", columnDefinition = "TEXT")
    private String documents; // JSON array of document URLs

    // System User Link
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user; // Link to user account for login

    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_parents",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id"),
            foreignKey = @ForeignKey(name = "fk_student_parents_student"),
            inverseForeignKey = @ForeignKey(name = "fk_student_parents_parent")
    )
    @Builder.Default
    private Set<Parent> parents = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_guardians",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "guardian_id")
    )
    @Builder.Default
    private Set<Parent> guardians = new HashSet<>();

    // Metadata
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "leaving_date")
    private LocalDate leavingDate;

    @Column(name = "leaving_reason", columnDefinition = "TEXT")
    private String leavingReason;

    // Business Methods
    public String getFullName() {
        StringBuilder name = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            name.append(firstName);
        }

        if (middleName != null && !middleName.isEmpty()) {
            if (!name.isEmpty()) name.append(" ");
            name.append(middleName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            if (!name.isEmpty()) name.append(" ");
            name.append(lastName);
        }

        return name.toString();
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    // Enums
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum StudentStatus {
        ACTIVE,      // Currently enrolled
        INACTIVE,    // Temporarily inactive
        GRADUATED,   // Completed education
        TRANSFERRED, // Transferred to another school
        DROPPED,     // Dropped out
        SUSPENDED,   // Temporarily suspended
        EXPELLED     // Expelled from school
    }
}