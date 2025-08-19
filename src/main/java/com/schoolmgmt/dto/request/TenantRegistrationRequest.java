package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.AdminUserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant registration request")
public class TenantRegistrationRequest {
    
    @NotBlank(message = "School name is required")
    @Size(min = 3, max = 200)
    @Schema(description = "Name of the school", example = "Greenwood International School")
    private String name;
    
    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain can only contain lowercase letters, numbers, and hyphens")
    @Schema(description = "Subdomain for school access", example = "greenwood")
    private String subdomain;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Primary contact email", example = "admin@greenwood.edu")
    private String email;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    @Schema(description = "Primary contact phone", example = "+1234567890")
    private String phone;
    
    @NotBlank(message = "Address is required")
    @Schema(description = "School address")
    private String address;
    
    @NotBlank(message = "City is required")
    @Schema(description = "City", example = "New York")
    private String city;
    
    @NotBlank(message = "State is required")
    @Schema(description = "State/Province", example = "NY")
    private String state;
    
    @NotBlank(message = "Country is required")
    @Schema(description = "Country", example = "USA")
    private String country;
    
    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Invalid postal code")
    @Schema(description = "Postal/ZIP code", example = "10001")
    private String postalCode;
    
    @NotBlank(message = "Subscription plan is required")
    @Pattern(regexp = "^(TRIAL|BASIC|STANDARD|PREMIUM|ENTERPRISE)$")
    @Schema(description = "Subscription plan", example = "TRIAL")
    private String subscriptionPlan;
    
    @Schema(description = "School website", example = "https://www.greenwood.edu")
    private String website;
    
    @NotNull(message = "Admin user details are required")
    @Schema(description = "Admin user details for the school")
    private AdminUserRequest adminUser;
    
    @Schema(description = "Additional configuration settings")
    private Map<String, String> configuration;
}
