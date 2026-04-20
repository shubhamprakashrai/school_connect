package com.schoolmgmt.dto.request;

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
@Schema(description = "Login request")
public class LoginRequest {

    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "admin@school.com", required = false)
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Schema(description = "Phone number", example = "9876543210", required = false)
    private String phone;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePass123!")
    private String password;

    @Schema(description = "Remember me flag for extended session", example = "false")
    private boolean rememberMe;

    /**
     * Validate that at least one of email or phone is provided
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return (email != null && !email.trim().isEmpty()) || (phone != null && !phone.trim().isEmpty());
    }
}
