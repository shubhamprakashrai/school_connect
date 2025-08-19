package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Update user request")
public class UpdateUserRequest {
    
    @Size(max = 100)
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Size(max = 100)
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;
    
    @URL
    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
