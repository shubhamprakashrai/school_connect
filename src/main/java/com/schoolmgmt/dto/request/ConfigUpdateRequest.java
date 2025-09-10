package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateRequest {
    private String schoolId;   // optional - if null, applies to all schools
    
    @NotBlank(message = "Scope is required")
    private String scope;      // e.g., "features"
    
    @NotBlank(message = "Key is required")
    private String key;        // e.g., "google_pay"
    
    private Object value;      // JSON object
}
