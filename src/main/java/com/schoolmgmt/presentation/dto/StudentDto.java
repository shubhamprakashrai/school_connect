package com.schoolmgmt.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Student related DTOs
 */
public class StudentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Student response")
    public static class StudentResponse {
        private String id;
        private String admissionNumber;
        private String rollNumber;
        private String firstName;
        private String middleName;
        private String lastName;
        private String fullName;
        private LocalDate dateOfBirth;
        private String gender;
        private String bloodGroup;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private String currentClassId;
        private String currentSectionId;
        private LocalDate admissionDate;
        private String status;
        private String photoUrl;
        private ParentInfo fatherInfo;
        private ParentInfo motherInfo;
        private ParentInfo guardianInfo;
        private EmergencyContact emergencyContact;
        private TransportInfo transportInfo;
        private Integer age;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create student request")
    public static class CreateStudentRequest {
        
        @NotBlank(message = "Admission number is required")
        @Size(max = 50)
        @Schema(description = "Unique admission number", example = "ADM2024001")
        private String admissionNumber;
        
        @NotBlank(message = "Roll number is required")
        @Size(max = 20)
        @Schema(description = "Roll number in class", example = "001")
        private String rollNumber;
        
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        @Schema(description = "First name", example = "John")
        private String firstName;
        
        @Size(max = 100)
        @Schema(description = "Middle name", example = "Michael")
        private String middleName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        @Schema(description = "Last name", example = "Doe")
        private String lastName;
        
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Date of birth", example = "2010-05-15")
        private LocalDate dateOfBirth;
        
        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
        @Schema(description = "Gender", example = "MALE")
        private String gender;
        
        @Schema(description = "Blood group", example = "O+")
        private String bloodGroup;
        
        @Email(message = "Invalid email format")
        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @Schema(description = "Address")
        private String address;
        
        @Schema(description = "City", example = "New York")
        private String city;
        
        @Schema(description = "State", example = "NY")
        private String state;
        
        @Schema(description = "Country", example = "USA")
        private String country;
        
        @Schema(description = "Postal code", example = "10001")
        private String postalCode;
        
        @NotBlank(message = "Class is required")
        @Schema(description = "Current class ID", example = "class_10")
        private String currentClassId;
        
        @NotBlank(message = "Section is required")
        @Schema(description = "Current section ID", example = "section_a")
        private String currentSectionId;
        
        @NotNull(message = "Admission date is required")
        @Schema(description = "Admission date", example = "2024-01-01")
        private LocalDate admissionDate;
        
        @Schema(description = "Previous school name")
        private String previousSchool;
        
        @Schema(description = "Father information")
        private ParentInfo fatherInfo;
        
        @Schema(description = "Mother information")
        private ParentInfo motherInfo;
        
        @Schema(description = "Guardian information")
        private ParentInfo guardianInfo;
        
        @NotNull(message = "Emergency contact is required")
        @Schema(description = "Emergency contact")
        private EmergencyContact emergencyContact;
        
        @Schema(description = "Medical information")
        private MedicalInfo medicalInfo;
        
        @Schema(description = "Transport information")
        private TransportInfo transportInfo;
        
        @Schema(description = "Fee category", example = "GENERAL")
        private String feeCategory;
        
        @Schema(description = "Scholarship applicable", example = "false")
        private Boolean scholarshipApplicable;
        
        @Schema(description = "Create user account for student", example = "true")
        private boolean createUserAccount = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update student request")
    public static class UpdateStudentRequest {
        
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
        
        @Schema(description = "Blood group")
        private String bloodGroup;
        
        @Schema(description = "Medical information")
        private MedicalInfo medicalInfo;
        
        @Schema(description = "Transport information")
        private TransportInfo transportInfo;
        
        @URL
        @Schema(description = "Photo URL")
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Parent information")
    public static class ParentInfo {
        
        @NotBlank(message = "Name is required")
        @Schema(description = "Parent name", example = "John Doe Sr.")
        private String name;
        
        @Schema(description = "Occupation", example = "Engineer")
        private String occupation;
        
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @Email
        @Schema(description = "Email address", example = "parent@example.com")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Emergency contact")
    public static class EmergencyContact {
        
        @NotBlank(message = "Name is required")
        @Schema(description = "Contact name", example = "Jane Doe")
        private String name;
        
        @NotBlank(message = "Relation is required")
        @Schema(description = "Relation to student", example = "Aunt")
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
    @Schema(description = "Medical information")
    public static class MedicalInfo {
        
        @Schema(description = "Medical conditions")
        private String medicalConditions;
        
        @Schema(description = "Allergies")
        private String allergies;
        
        @Schema(description = "Emergency medication")
        private String emergencyMedication;
        
        @Schema(description = "Doctor name")
        private String doctorName;
        
        @Schema(description = "Doctor phone")
        private String doctorPhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Transport information")
    public static class TransportInfo {
        
        @Schema(description = "Transport mode", example = "BUS")
        private String transportMode;
        
        @Schema(description = "Transport route ID")
        private String transportRouteId;
        
        @Schema(description = "Pickup point")
        private String pickupPoint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Student filter request")
    public static class StudentFilterRequest {
        
        @Schema(description = "Filter by class ID")
        private String classId;
        
        @Schema(description = "Filter by section ID")
        private String sectionId;
        
        @Schema(description = "Filter by status")
        private String status;
        
        @Schema(description = "Search by name or admission number")
        private String search;
        
        @Schema(description = "Filter by gender")
        private String gender;
        
        @Schema(description = "Filter by fee category")
        private String feeCategory;
        
        @Schema(description = "Filter by transport route")
        private String transportRouteId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Promote student request")
    public static class PromoteStudentRequest {
        
        @NotBlank(message = "New class is required")
        @Schema(description = "New class ID")
        private String newClassId;
        
        @NotBlank(message = "New section is required")
        @Schema(description = "New section ID")
        private String newSectionId;
        
        @Schema(description = "New roll number")
        private String newRollNumber;
        
        @NotBlank(message = "Promotion status is required")
        @Pattern(regexp = "^(PROMOTED|DETAINED)$")
        @Schema(description = "Promotion status", example = "PROMOTED")
        private String promotionStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Student statistics")
    public static class StudentStatistics {
        private long totalStudents;
        private long activeStudents;
        private long maleStudents;
        private long femaleStudents;
        private Map<String, Long> studentsByClass;
        private Map<String, Long> studentsByStatus;
    }
}
