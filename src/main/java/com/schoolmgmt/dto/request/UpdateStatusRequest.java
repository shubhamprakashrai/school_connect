package com.schoolmgmt.dto.request;

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
@Schema(description = "Update status request")
public class UpdateStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PENDING|ACTIVE|INACTIVE|SUSPENDED|DELETED)$")
    @Schema(description = "User status", example = "ACTIVE")
    private String status;
}
