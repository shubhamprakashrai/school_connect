package com.schoolmgmt.dto.request;

import lombok.Data;

@Data
public class ConfigUpdateRequest {
    private String schoolId;   // optional
    private String scope;      // e.g., "features"
    private String key;        // e.g., "google_pay"
    private Object value;      // JSON object
}
