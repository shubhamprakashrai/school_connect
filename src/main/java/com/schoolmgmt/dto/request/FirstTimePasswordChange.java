package com.schoolmgmt.dto.request;




import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset request at first time")
public class FirstTimePasswordChange {

    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password")
    private String currentPassword;


    @NotBlank(message = "User name is required")
    @Schema(description = "User Name")
    private String username;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
    @Schema(description = "New password", example = "NewSecurePass123!")
    private String newPassword;
}


