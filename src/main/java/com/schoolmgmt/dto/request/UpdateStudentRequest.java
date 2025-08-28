package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.MedicalInfo;
import com.schoolmgmt.dto.common.TransportInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update student request")
public class UpdateStudentRequest {
    
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
    
    @Schema(description = "Medical information")
    private MedicalInfo medicalInfo;
    
    @URL
    @Schema(description = "Photo URL")
    private String photoUrl;
}
