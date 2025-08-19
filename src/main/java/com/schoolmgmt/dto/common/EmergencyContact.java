package com.schoolmgmt.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Emergency contact")
public class EmergencyContact {
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Contact name", example = "Jane Doe")
    private String name;
    
    @NotBlank(message = "Relation is required")
    @Schema(description = "Relation to student", example = "Aunt")
    private String relation;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;
}
