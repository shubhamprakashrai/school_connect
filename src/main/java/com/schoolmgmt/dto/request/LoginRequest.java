package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Username or email", example = "admin@school.com")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePass123!")
    private String password;
    
    @NotBlank(message = "Tenant ID is required")
    @Schema(description = "Tenant identifier", example = "public")
    private String tenantId;
    
    @Schema(description = "Remember me flag for extended session", example = "false")
    private boolean rememberMe;
}
