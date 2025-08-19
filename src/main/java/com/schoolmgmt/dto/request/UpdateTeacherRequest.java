package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update teacher request")
public class UpdateTeacherRequest {
    
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
