package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TenantSettingsRequest;
import com.schoolmgmt.dto.response.TenantSettingsResponse;
import com.schoolmgmt.exception.TenantNotFoundException;
import com.schoolmgmt.model.TenantSettings;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.TenantSettingsRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantSettingsService {

    private final TenantSettingsRepository settingsRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get settings for the current tenant
     */
    @Transactional(readOnly = true)
    public TenantSettingsResponse getCurrentTenantSettings() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context found");
        }
        return getSettingsByTenantId(tenantId);
    }

    /**
     * Get settings by tenant ID
     */
    @Transactional(readOnly = true)
    public TenantSettingsResponse getSettingsByTenantId(String tenantId) {
        log.info("Fetching settings for tenant: {}", tenantId);

        TenantSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));

        return TenantSettingsResponse.fromEntity(settings);
    }

    /**
     * Update settings for the current tenant
     */
    @Transactional
    public TenantSettingsResponse updateCurrentTenantSettings(TenantSettingsRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context found");
        }
        return updateSettings(tenantId, request);
    }

    /**
     * Update settings for a specific tenant
     */
    @Transactional
    public TenantSettingsResponse updateSettings(String tenantId, TenantSettingsRequest request) {
        log.info("Updating settings for tenant: {}", tenantId);

        // Verify tenant exists
        if (!tenantRepository.existsById(UUID.fromString(tenantId))) {
            throw new TenantNotFoundException("Tenant not found: " + tenantId);
        }

        TenantSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));

        // Update branding
        if (request.getDisplayName() != null) {
            settings.setDisplayName(request.getDisplayName());
        }
        if (request.getTagline() != null) {
            settings.setTagline(request.getTagline());
        }
        if (request.getLogoUrl() != null) {
            settings.setLogoUrl(request.getLogoUrl());
        }
        if (request.getPrimaryColor() != null) {
            settings.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            settings.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getAccentColor() != null) {
            settings.setAccentColor(request.getAccentColor());
        }

        // Update academic settings
        if (request.getAcademicYearStart() != null) {
            settings.setAcademicYearStart(request.getAcademicYearStart());
        }
        if (request.getAcademicYearEnd() != null) {
            settings.setAcademicYearEnd(request.getAcademicYearEnd());
        }
        if (request.getGradingSystem() != null) {
            settings.setGradingSystem(request.getGradingSystem());
        }
        if (request.getPassingPercentage() != null) {
            settings.setPassingPercentage(request.getPassingPercentage());
        }
        if (request.getDefaultWorkingDays() != null) {
            settings.setDefaultWorkingDays(request.getDefaultWorkingDays());
        }
        if (request.getSchoolStartTime() != null) {
            settings.setSchoolStartTime(request.getSchoolStartTime());
        }
        if (request.getSchoolEndTime() != null) {
            settings.setSchoolEndTime(request.getSchoolEndTime());
        }

        // Update feature flags
        if (request.getAttendanceEnabled() != null) {
            settings.setAttendanceEnabled(request.getAttendanceEnabled());
        }
        if (request.getFeesEnabled() != null) {
            settings.setFeesEnabled(request.getFeesEnabled());
        }
        if (request.getExamsEnabled() != null) {
            settings.setExamsEnabled(request.getExamsEnabled());
        }
        if (request.getTimetableEnabled() != null) {
            settings.setTimetableEnabled(request.getTimetableEnabled());
        }
        if (request.getLibraryEnabled() != null) {
            settings.setLibraryEnabled(request.getLibraryEnabled());
        }
        if (request.getTransportEnabled() != null) {
            settings.setTransportEnabled(request.getTransportEnabled());
        }
        if (request.getHostelEnabled() != null) {
            settings.setHostelEnabled(request.getHostelEnabled());
        }
        if (request.getParentPortalEnabled() != null) {
            settings.setParentPortalEnabled(request.getParentPortalEnabled());
        }
        if (request.getStudentPortalEnabled() != null) {
            settings.setStudentPortalEnabled(request.getStudentPortalEnabled());
        }
        if (request.getSmsNotificationsEnabled() != null) {
            settings.setSmsNotificationsEnabled(request.getSmsNotificationsEnabled());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            settings.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getPushNotificationsEnabled() != null) {
            settings.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
        }

        // Update locale settings
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }
        if (request.getTimeFormat() != null) {
            settings.setTimeFormat(request.getTimeFormat());
        }
        if (request.getCurrency() != null) {
            settings.setCurrency(request.getCurrency());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }

        // Update contact settings
        if (request.getSupportEmail() != null) {
            settings.setSupportEmail(request.getSupportEmail());
        }
        if (request.getSupportPhone() != null) {
            settings.setSupportPhone(request.getSupportPhone());
        }
        if (request.getEmergencyContact() != null) {
            settings.setEmergencyContact(request.getEmergencyContact());
        }

        TenantSettings savedSettings = settingsRepository.save(settings);
        log.info("Settings updated for tenant: {}", tenantId);

        return TenantSettingsResponse.fromEntity(savedSettings);
    }

    /**
     * Update branding only
     */
    @Transactional
    public TenantSettingsResponse updateBranding(String tenantId, TenantSettingsRequest request) {
        log.info("Updating branding for tenant: {}", tenantId);

        TenantSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));

        if (request.getDisplayName() != null) {
            settings.setDisplayName(request.getDisplayName());
        }
        if (request.getTagline() != null) {
            settings.setTagline(request.getTagline());
        }
        if (request.getLogoUrl() != null) {
            settings.setLogoUrl(request.getLogoUrl());
        }
        if (request.getPrimaryColor() != null) {
            settings.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            settings.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getAccentColor() != null) {
            settings.setAccentColor(request.getAccentColor());
        }

        TenantSettings savedSettings = settingsRepository.save(settings);
        return TenantSettingsResponse.fromEntity(savedSettings);
    }

    /**
     * Update feature flags only
     */
    @Transactional
    public TenantSettingsResponse updateFeatureFlags(String tenantId, TenantSettingsRequest request) {
        log.info("Updating feature flags for tenant: {}", tenantId);

        TenantSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));

        if (request.getAttendanceEnabled() != null) {
            settings.setAttendanceEnabled(request.getAttendanceEnabled());
        }
        if (request.getFeesEnabled() != null) {
            settings.setFeesEnabled(request.getFeesEnabled());
        }
        if (request.getExamsEnabled() != null) {
            settings.setExamsEnabled(request.getExamsEnabled());
        }
        if (request.getTimetableEnabled() != null) {
            settings.setTimetableEnabled(request.getTimetableEnabled());
        }
        if (request.getLibraryEnabled() != null) {
            settings.setLibraryEnabled(request.getLibraryEnabled());
        }
        if (request.getTransportEnabled() != null) {
            settings.setTransportEnabled(request.getTransportEnabled());
        }
        if (request.getHostelEnabled() != null) {
            settings.setHostelEnabled(request.getHostelEnabled());
        }
        if (request.getParentPortalEnabled() != null) {
            settings.setParentPortalEnabled(request.getParentPortalEnabled());
        }
        if (request.getStudentPortalEnabled() != null) {
            settings.setStudentPortalEnabled(request.getStudentPortalEnabled());
        }

        TenantSettings savedSettings = settingsRepository.save(settings);
        return TenantSettingsResponse.fromEntity(savedSettings);
    }

    /**
     * Create default settings for a tenant
     */
    private TenantSettings createDefaultSettings(String tenantId) {
        log.info("Creating default settings for tenant: {}", tenantId);

        TenantSettings settings = TenantSettings.builder()
                .tenantId(tenantId)
                .build();

        return settingsRepository.save(settings);
    }

    /**
     * Delete settings for a tenant
     */
    @Transactional
    public void deleteSettings(String tenantId) {
        log.info("Deleting settings for tenant: {}", tenantId);
        settingsRepository.deleteByTenantId(tenantId);
    }
}
