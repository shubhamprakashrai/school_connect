package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Teacher entity representing a teacher/faculty member in the school system.
 */
@Entity
@Table(name = "teachers",
       indexes = {
           @Index(name = "idx_teacher_employee_id", columnList = "employee_id, tenant_id", unique = true),
           @Index(name = "idx_teacher_email", columnList = "email, tenant_id", unique = true),
           @Index(name = "idx_teacher_status", columnList = "status"),
           @Index(name = "idx_teacher_department", columnList = "department")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user", "subjects", "classes"})
public class Teacher extends BaseEntity {

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId; // Unique employee ID

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "religion", length = 50)
    private String religion;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    // Contact Information
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Professional Information
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 20)
    @Builder.Default
    private EmployeeType employeeType = EmployeeType.PERMANENT;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @ElementCollection
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject")
    @Builder.Default
    private Set<String> subjects = new HashSet<>(); // Subjects the teacher can teach

    @ElementCollection
    @CollectionTable(name = "teacher_classes", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "class_id")
    @Builder.Default
    private Set<String> classes = new HashSet<>(); // Classes assigned to teacher

    @Column(name = "is_class_teacher")
    @Builder.Default
    private Boolean isClassTeacher = false;

    @Column(name = "class_teacher_for", length = 50)
    private String classTeacherFor; // Class ID if class teacher

    // Qualifications
    @Column(name = "highest_qualification", length = 100)
    private String highestQualification;

    @Column(name = "professional_qualification", length = 100)
    private String professionalQualification;

    @ElementCollection
    @CollectionTable(name = "teacher_qualifications", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "qualification")
    @Builder.Default
    private Set<String> qualifications = new HashSet<>();

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "previous_school", length = 200)
    private String previousSchool;

    @ElementCollection
    @CollectionTable(name = "teacher_specializations", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "specialization")
    @Builder.Default
    private Set<String> specializations = new HashSet<>();

    // Salary Information
    @Column(name = "basic_salary", precision = 10, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "gross_salary", precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "pf_number", length = 50)
    private String pfNumber;

    @Column(name = "esi_number", length = 50)
    private String esiNumber;

    // Status and Availability
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TeacherStatus status = TeacherStatus.ACTIVE;

    @Column(name = "leaving_date")
    private LocalDate leavingDate;

    @Column(name = "leaving_reason", columnDefinition = "TEXT")
    private String leavingReason;

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    // Documents
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "documents", columnDefinition = "TEXT")
    private String documents; // JSON array of document URLs

    // System User Link
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user; // Link to user account for login

    // Additional Information
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "publications", columnDefinition = "TEXT")
    private String publications;

    @Column(name = "research_interests", columnDefinition = "TEXT")
    private String researchInterests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Performance Metrics
    @Column(name = "rating")
    private Double rating; // Performance rating

    @Column(name = "attendance_percentage")
    private Double attendancePercentage;

    @Column(name = "last_evaluation_date")
    private LocalDate lastEvaluationDate;

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
        return status == TeacherStatus.ACTIVE;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public int getServiceYears() {
        return LocalDate.now().getYear() - joiningDate.getYear();
    }

    public boolean canTeachSubject(String subject) {
        return subjects.contains(subject);
    }

    public boolean isAssignedToClass(String classId) {
        return classes.contains(classId);
    }

    // Enums
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum MaritalStatus {
        SINGLE,
        MARRIED,
        DIVORCED,
        WIDOWED,
        OTHER
    }

    public enum EmployeeType {
        PERMANENT,      // Permanent employee
        CONTRACT,       // Contract basis
        TEMPORARY,      // Temporary
        PROBATION,      // On probation
        VISITING,       // Visiting faculty
        VOLUNTEER       // Volunteer teacher
    }

    public enum TeacherStatus {
        ACTIVE,         // Currently teaching
        ON_LEAVE,       // On leave
        INACTIVE,       // Temporarily inactive
        RESIGNED,       // Resigned
        TERMINATED,     // Terminated
        RETIRED,        // Retired
        TRANSFERRED     // Transferred
    }
}
