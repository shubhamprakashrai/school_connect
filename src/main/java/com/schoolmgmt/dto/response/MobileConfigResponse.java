package com.schoolmgmt.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MobileConfigResponse {
    private String version;
    private String lastUpdated;
    private Map<String, Object> features;
    private Map<String, Object> ui;
    private Map<String, Object> runtime;
}
