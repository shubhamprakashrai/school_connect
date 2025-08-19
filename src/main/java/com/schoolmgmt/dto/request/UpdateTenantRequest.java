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

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update tenant request")
public class UpdateTenantRequest {
    
    @Size(min = 3, max = 200)
    @Schema(description = "School name")
    private String name;
    
    @Email(message = "Invalid email format")
    @Schema(description = "Contact email")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Contact phone")
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
    
    @URL
    @Schema(description = "Website URL")
    private String website;
    
    @URL
    @Schema(description = "Logo URL")
    private String logoUrl;
    
    @Schema(description = "Configuration settings")
    private Map<String, String> configuration;
}
