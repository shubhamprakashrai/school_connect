package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User filter request")
public class UserFilterRequest {
    
    @Schema(description = "Filter by role", example = "TEACHER")
    private String role;
    
    @Schema(description = "Filter by status", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Search by name or email", example = "john")
    private String search;
    
    @Schema(description = "Filter by email verified", example = "true")
    private Boolean emailVerified;
    
    @Schema(description = "Filter by MFA enabled", example = "false")
    private Boolean mfaEnabled;
}
