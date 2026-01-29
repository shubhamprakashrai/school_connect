package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for platform configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformConfigResponse {

    private String id;

    // Branding
    private String appName;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;

    // Contact
    private String supportEmail;
    private String supportPhone;
    private String websiteUrl;

    // Legal Links
    private String termsUrl;
    private String privacyUrl;

    // Feature Flags
    private Map<String, Boolean> featureFlags;

    // Social Links
    private Map<String, String> socialLinks;

    // Maintenance Mode
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private LocalDateTime maintenanceEndTime;

    // App Versions
    private String minAndroidVersion;
    private String minIosVersion;
    private String latestAndroidVersion;
    private String latestIosVersion;
    private Boolean forceUpdate;
    private String updateMessage;

    // Metadata
    private LocalDateTime updatedAt;
    private String updatedBy;
}
