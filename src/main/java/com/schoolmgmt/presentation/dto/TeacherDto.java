package com.schoolmgmt.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Teacher related DTOs
 */
public class TeacherDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Teacher response")
    public static class TeacherResponse {
        private String id;
        private String employeeId;
        private String firstName;
        private String middleName;
        private String lastName;
        private String fullName;
        private LocalDate dateOfBirth;
        private String gender;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private LocalDate joiningDate;
        private String employeeType;
        private String department;
        private String designation;
        private Set<String> subjects;
        private Set<String> classes;
        private Boolean isClassTeacher;
        private String classTeacherFor;
        private String highestQualification;
        private Integer experienceYears;
        private String status;
        private String photoUrl;
        private Double rating;
        private Integer age;
        private Integer serviceYears;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create teacher request")
    public static class CreateTeacherRequest {
        
        @NotBlank(message = "Employee ID is required")
        @Size(max = 50)
        @Schema(description = "Unique employee ID", example = "EMP2024001")
        private String employeeId;
        
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        @Schema(description = "First name", example = "Jane")
        private String firstName;
        
        @Size(max = 100)
        @Schema(description = "Middle name")
        private String middleName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        @Schema(description = "Last name", example = "Smith")
        private String lastName;
        
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Date of birth", example = "1985-03-20")
        private LocalDate dateOfBirth;
        
        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
        @Schema(description = "Gender", example = "FEMALE")
        private String gender;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "jane.smith@school.com")
        private String email;
        
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
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
        
        @NotNull(message = "Joining date is required")
        @Schema(description = "Joining date", example = "2020-01-01")
        private LocalDate joiningDate;
        
        @NotBlank(message = "Employee type is required")
        @Pattern(regexp = "^(PERMANENT|CONTRACT|TEMPORARY|PROBATION|VISITING|VOLUNTEER)$")
        @Schema(description = "Employee type", example = "PERMANENT")
        private String employeeType;
        
        @Schema(description = "Department", example = "Mathematics")
        private String department;
        
        @NotBlank(message = "Designation is required")
        @Schema(description = "Designation", example = "Senior Teacher")
        private String designation;
        
        @Schema(description = "Subjects teacher can teach")
        private Set<String> subjects;
        
        @Schema(description = "Highest qualification", example = "M.Sc Mathematics")
        private String highestQualification;
        
        @Schema(description = "Professional qualification", example = "B.Ed")
        private String professionalQualification;
        
        @Schema(description = "Years of experience")
        @Min(0)
        private Integer experienceYears;
        
        @Schema(description = "Previous school")
        private String previousSchool;
        
        @Schema(description = "Specializations")
        private Set<String> specializations;
        
        @Schema(description = "Basic salary")
        @DecimalMin(value = "0.0")
        private BigDecimal basicSalary;
        
        @Schema(description = "Bank account details")
        private BankDetails bankDetails;
        
        @Schema(description = "Emergency contact")
        private EmergencyContact emergencyContact;
        
        @Schema(description = "Create user account for teacher", example = "true")
        private boolean createUserAccount = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update teacher request")
    public static class UpdateTeacherRequest {
        
        @Size(max = 100)
        @Schema(description = "First name")
        private String firstName;
        
        @Size(max = 100)
        @Schema(description = "Middle name")
        private String middleName;
        
        @Size(max = 100)
        @Schema(description = "Last name")
        private String lastName;
        
        @Email(message = "Invalid email format")
        @Schema(description = "Email address")
        private String email;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number")
        private String phone;
        
        @Schema(description = "Address")
        private String address;
        
        @Schema(description = "City")
        private String city;
        
        @Schema(description = "State")
        private String state;
        
        @Schema(description = "Postal code")
        private String postalCode;
        
        @Schema(description = "Department")
        private String department;
        
        @Schema(description = "Designation")
        private String designation;
        
        @Schema(description = "Subjects")
        private Set<String> subjects;
        
        @Schema(description = "Qualifications")
        private Set<String> qualifications;
        
        @URL
        @Schema(description = "Photo URL")
        private String photoUrl;
        
        @URL
        @Schema(description = "Resume URL")
        private String resumeUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Bank details")
    public static class BankDetails {
        
        @Schema(description = "Bank name", example = "State Bank")
        private String bankName;
        
        @Schema(description = "Account number", example = "1234567890")
        private String accountNumber;
        
        @Schema(description = "Bank branch", example = "Main Branch")
        private String branch;
        
        @Schema(description = "IFSC code", example = "SBIN0001234")
        private String ifscCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Emergency contact")
    public static class EmergencyContact {
        
        @NotBlank(message = "Name is required")
        @Schema(description = "Contact name", example = "John Doe")
        private String name;
        
        @NotBlank(message = "Relation is required")
        @Schema(description = "Relation", example = "Spouse")
        private String relation;
        
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Assign class teacher request")
    public static class AssignClassTeacherRequest {
        
        @NotBlank(message = "Class ID is required")
        @Schema(description = "Class ID to assign", example = "class_10_a")
        private String classId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Teacher filter request")
    public static class TeacherFilterRequest {
        
        @Schema(description = "Filter by department")
        private String department;
        
        @Schema(description = "Filter by designation")
        private String designation;
        
        @Schema(description = "Filter by status")
        private String status;
        
        @Schema(description = "Filter by employee type")
        private String employeeType;
        
        @Schema(description = "Filter by subject")
        private String subject;
        
        @Schema(description = "Search by name or employee ID")
        private String search;
        
        @Schema(description = "Filter class teachers only")
        private Boolean classTeachersOnly;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Teacher statistics")
    public static class TeacherStatistics {
        private long totalTeachers;
        private long activeTeachers;
        private Map<String, Long> teachersByDepartment;
        private Map<String, Long> teachersByEmployeeType;
        private Map<String, Double> averageRatingByDepartment;
        private long classTeachers;
        private double averageExperience;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update teacher rating request")
    public static class UpdateRatingRequest {
        
        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "5.0")
        @Schema(description = "Rating (0-5)", example = "4.5")
        private Double rating;
        
        @Schema(description = "Evaluation comments")
        private String comments;
    }
}
