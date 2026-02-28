package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.*;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create student request")
public class CreateStudentRequest {
    
//    @NotBlank(message = "Roll number is required")
    @Size(max = 20)
    @Schema(description = "Roll number in class", example = "001")
    private String rollNumber;

    @NotNull(message = "user details are required")
    @Schema(description = "user details for the school")
    private UserRequest userRequest;


//    @NotBlank(message = "First name is required")
//    @Size(max = 100)
//    @Schema(description = "First name", example = "John")
//    private String firstName;
//
//    @Size(max = 100)
//    @Schema(description = "Middle name", example = "Michael")
//    private String middleName;
//
//    @NotBlank(message = "Last name is required")
//    @Size(max = 100)
//    @Schema(description = "Last name", example = "Doe")
//    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "2010-05-15")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
    @Schema(description = "Gender", example = "MALE")
    private String gender;
    

    
//    @Email(message = "Invalid email format")
//    @Schema(description = "Email address", example = "john.doe@example.com")
//    private String email;
    
//    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
//    @Schema(description = "Phone number", example = "+1234567890")
//    private String phone;
//
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
    
//    @NotBlank(message = "Class is required")
//    @Schema(description = "Current class ID", example = "class_10")
//    private String currentClassId;
//
//    @NotBlank(message = "Section is required")
//    @Schema(description = "Current section ID", example = "section_a")
//    private String currentSectionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_section_id", nullable = false)
    private Section section;

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
    

    
    @Schema(description = "Create user account for student", example = "true")
    private boolean createUserAccount = false;
}
