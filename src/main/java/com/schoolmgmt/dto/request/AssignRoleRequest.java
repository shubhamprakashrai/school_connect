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
@Schema(description = "Assign role request")
public class AssignRoleRequest {
    
    @NotBlank(message = "Role is required")
    @Schema(description = "Role to assign", example = "TEACHER")
    private String role;
}
