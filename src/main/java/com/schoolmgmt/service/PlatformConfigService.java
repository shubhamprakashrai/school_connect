package com.schoolmgmt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.dto.request.PlatformConfigRequest;
import com.schoolmgmt.dto.response.PlatformConfigResponse;
import com.schoolmgmt.model.PlatformConfig;
import com.schoolmgmt.repository.PlatformConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing platform configuration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformConfigService {

    private static final String DEFAULT_CONFIG_KEY = "default";

    private final PlatformConfigRepository platformConfigRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get platform configuration
     */
    public PlatformConfigResponse getConfig() {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        return toResponse(config);
    }

    /**
     * Get mobile app configuration (public endpoint)
     */
    public Map<String, Object> getMobileConfig() {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        Map<String, Object> mobileConfig = new HashMap<>();

        // Branding
        mobileConfig.put("appName", config.getAppName());
        mobileConfig.put("logoUrl", config.getLogoUrl());
        mobileConfig.put("primaryColor", config.getPrimaryColor());
        mobileConfig.put("secondaryColor", config.getSecondaryColor());
        mobileConfig.put("accentColor", config.getAccentColor());

        // Contact
        mobileConfig.put("supportEmail", config.getSupportEmail());
        mobileConfig.put("supportPhone", config.getSupportPhone());

        // Links
        mobileConfig.put("termsUrl", config.getTermsUrl());
        mobileConfig.put("privacyUrl", config.getPrivacyUrl());

        // Feature flags
        mobileConfig.put("features", parseJson(config.getFeatureFlags(), new HashMap<String, Boolean>()));

        // Maintenance
        mobileConfig.put("maintenanceMode", config.getMaintenanceMode());
        if (config.getMaintenanceMode()) {
            mobileConfig.put("maintenanceMessage", config.getMaintenanceMessage());
            mobileConfig.put("maintenanceEndTime", config.getMaintenanceEndTime());
        }

        // App versions
        mobileConfig.put("minAndroidVersion", config.getMinAndroidVersion());
        mobileConfig.put("minIosVersion", config.getMinIosVersion());
        mobileConfig.put("latestAndroidVersion", config.getLatestAndroidVersion());
        mobileConfig.put("latestIosVersion", config.getLatestIosVersion());
        mobileConfig.put("forceUpdate", config.getForceUpdate());
        mobileConfig.put("updateMessage", config.getUpdateMessage());

        return mobileConfig;
    }

    /**
     * Update platform configuration
     */
    public PlatformConfigResponse updateConfig(PlatformConfigRequest request, String updatedBy) {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        updateConfigFields(config, request);
        config.setUpdatedBy(updatedBy);

        PlatformConfig savedConfig = platformConfigRepository.save(config);
        log.info("Platform config updated by: {}", updatedBy);

        return toResponse(savedConfig);
    }

    /**
     * Update feature flags
     */
    public PlatformConfigResponse updateFeatureFlags(Map<String, Boolean> featureFlags, String updatedBy) {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        config.setFeatureFlags(toJson(featureFlags));
        config.setUpdatedBy(updatedBy);

        PlatformConfig savedConfig = platformConfigRepository.save(config);
        log.info("Feature flags updated by: {}", updatedBy);

        return toResponse(savedConfig);
    }

    /**
     * Update branding
     */
    public PlatformConfigResponse updateBranding(PlatformConfigRequest request, String updatedBy) {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        if (request.getAppName() != null) config.setAppName(request.getAppName());
        if (request.getLogoUrl() != null) config.setLogoUrl(request.getLogoUrl());
        if (request.getFaviconUrl() != null) config.setFaviconUrl(request.getFaviconUrl());
        if (request.getPrimaryColor() != null) config.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) config.setSecondaryColor(request.getSecondaryColor());
        if (request.getAccentColor() != null) config.setAccentColor(request.getAccentColor());

        config.setUpdatedBy(updatedBy);
        PlatformConfig savedConfig = platformConfigRepository.save(config);
        log.info("Branding updated by: {}", updatedBy);

        return toResponse(savedConfig);
    }

    /**
     * Set maintenance mode
     */
    public PlatformConfigResponse setMaintenanceMode(boolean enabled, String message, String updatedBy) {
        PlatformConfig config = platformConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);

        config.setMaintenanceMode(enabled);
        config.setMaintenanceMessage(message);
        config.setUpdatedBy(updatedBy);

        PlatformConfig savedConfig = platformConfigRepository.save(config);
        log.info("Maintenance mode set to {} by: {}", enabled, updatedBy);

        return toResponse(savedConfig);
    }

    // Private helper methods

    private PlatformConfig createDefaultConfig() {
        PlatformConfig config = PlatformConfig.builder()
                .configKey(DEFAULT_CONFIG_KEY)
                .appName("School Connect")
                .primaryColor("#1976D2")
                .secondaryColor("#424242")
                .accentColor("#FF9800")
                .maintenanceMode(false)
                .forceUpdate(false)
                .featureFlags(toJson(getDefaultFeatureFlags()))
                .build();

        return platformConfigRepository.save(config);
    }

    private Map<String, Boolean> getDefaultFeatureFlags() {
        Map<String, Boolean> flags = new HashMap<>();
        flags.put("attendance", true);
        flags.put("fees", true);
        flags.put("exams", true);
        flags.put("timetable", true);
        flags.put("notifications", true);
        flags.put("parentPortal", true);
        flags.put("library", false);
        flags.put("transport", false);
        flags.put("hostel", false);
        flags.put("onlineClasses", false);
        flags.put("smsNotifications", false);
        return flags;
    }

    private void updateConfigFields(PlatformConfig config, PlatformConfigRequest request) {
        if (request.getAppName() != null) config.setAppName(request.getAppName());
        if (request.getLogoUrl() != null) config.setLogoUrl(request.getLogoUrl());
        if (request.getFaviconUrl() != null) config.setFaviconUrl(request.getFaviconUrl());
        if (request.getPrimaryColor() != null) config.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) config.setSecondaryColor(request.getSecondaryColor());
        if (request.getAccentColor() != null) config.setAccentColor(request.getAccentColor());
        if (request.getSupportEmail() != null) config.setSupportEmail(request.getSupportEmail());
        if (request.getSupportPhone() != null) config.setSupportPhone(request.getSupportPhone());
        if (request.getWebsiteUrl() != null) config.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getTermsUrl() != null) config.setTermsUrl(request.getTermsUrl());
        if (request.getPrivacyUrl() != null) config.setPrivacyUrl(request.getPrivacyUrl());
        if (request.getFeatureFlags() != null) config.setFeatureFlags(toJson(request.getFeatureFlags()));
        if (request.getSocialLinks() != null) config.setSocialLinks(toJson(request.getSocialLinks()));
        if (request.getMaintenanceMode() != null) config.setMaintenanceMode(request.getMaintenanceMode());
        if (request.getMaintenanceMessage() != null) config.setMaintenanceMessage(request.getMaintenanceMessage());
        if (request.getMaintenanceEndTime() != null) config.setMaintenanceEndTime(request.getMaintenanceEndTime());
        if (request.getMinAndroidVersion() != null) config.setMinAndroidVersion(request.getMinAndroidVersion());
        if (request.getMinIosVersion() != null) config.setMinIosVersion(request.getMinIosVersion());
        if (request.getLatestAndroidVersion() != null) config.setLatestAndroidVersion(request.getLatestAndroidVersion());
        if (request.getLatestIosVersion() != null) config.setLatestIosVersion(request.getLatestIosVersion());
        if (request.getForceUpdate() != null) config.setForceUpdate(request.getForceUpdate());
        if (request.getUpdateMessage() != null) config.setUpdateMessage(request.getUpdateMessage());
    }

    private PlatformConfigResponse toResponse(PlatformConfig config) {
        return PlatformConfigResponse.builder()
                .id(config.getId().toString())
                .appName(config.getAppName())
                .logoUrl(config.getLogoUrl())
                .faviconUrl(config.getFaviconUrl())
                .primaryColor(config.getPrimaryColor())
                .secondaryColor(config.getSecondaryColor())
                .accentColor(config.getAccentColor())
                .supportEmail(config.getSupportEmail())
                .supportPhone(config.getSupportPhone())
                .websiteUrl(config.getWebsiteUrl())
                .termsUrl(config.getTermsUrl())
                .privacyUrl(config.getPrivacyUrl())
                .featureFlags(parseJson(config.getFeatureFlags(), new HashMap<>()))
                .socialLinks(parseJsonString(config.getSocialLinks(), new HashMap<>()))
                .maintenanceMode(config.getMaintenanceMode())
                .maintenanceMessage(config.getMaintenanceMessage())
                .maintenanceEndTime(config.getMaintenanceEndTime())
                .minAndroidVersion(config.getMinAndroidVersion())
                .minIosVersion(config.getMinIosVersion())
                .latestAndroidVersion(config.getLatestAndroidVersion())
                .latestIosVersion(config.getLatestIosVersion())
                .forceUpdate(config.getForceUpdate())
                .updateMessage(config.getUpdateMessage())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseJson(String json, T defaultValue) {
        if (json == null || json.isEmpty()) {
            return defaultValue;
        }
        try {
            return (T) objectMapper.readValue(json, new TypeReference<Map<String, Boolean>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON", e);
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseJsonString(String json, T defaultValue) {
        if (json == null || json.isEmpty()) {
            return defaultValue;
        }
        try {
            return (T) objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON", e);
            return defaultValue;
        }
    }
}
