package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteConfigRequest {
    private String schoolId; // Optional: null for global config
    
    @NotBlank(message = "Scope is required")
    private String scope;
    
    @NotBlank(message = "Key is required")
    private String key;
}
