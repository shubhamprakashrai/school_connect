package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.BankDetails;
import com.schoolmgmt.dto.common.EmergencyContact;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create teacher request")
public class CreateTeacherRequest {
    
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
