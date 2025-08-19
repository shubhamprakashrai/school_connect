package com.schoolmgmt.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parent information")
public class ParentInfo {
    
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
