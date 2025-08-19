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
@Schema(description = "Tenant activation request")
public class TenantActivationRequest {
    
    @NotBlank(message = "Activation code is required")
    @Schema(description = "Activation code sent via email")
    private String activationCode;
}
