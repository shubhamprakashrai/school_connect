package com.schoolmgmt.dto.response;

import com.schoolmgmt.dto.common.AdminInfo;
import com.schoolmgmt.dto.common.TenantInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant registration response")
public class TenantRegistrationResponse {
    
    @Schema(description = "Tenant details")
    private TenantInfo tenant;
    
    @Schema(description = "Admin user details")
    private AdminInfo admin;
    
    @Schema(description = "Success message")
    private String message;
    
    @Schema(description = "Access URL for the tenant")
    private String accessUrl;
    
    @Schema(description = "Next steps for setup")
    private String[] nextSteps;
}
