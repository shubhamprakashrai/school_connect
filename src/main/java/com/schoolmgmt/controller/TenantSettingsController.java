package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.TenantSettingsRequest;
import com.schoolmgmt.dto.response.TenantSettingsResponse;
import com.schoolmgmt.service.TenantSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for tenant settings management.
 * Allows school admins to customize their school's configuration.
 */
@RestController
@RequestMapping("/api/tenant/settings")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tenant Settings", description = "School-specific settings and configuration APIs")
public class TenantSettingsController {

    private final TenantSettingsService settingsService;

    /**
     * Get settings for the current tenant (authenticated user's school)
     */
    @GetMapping
    @Operation(summary = "Get tenant settings", description = "Get settings for the current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> getCurrentSettings() {
        log.info("Fetching settings for current tenant");
        TenantSettingsResponse settings = settingsService.getCurrentTenantSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Update settings for the current tenant
     */
    @PutMapping
    @Operation(summary = "Update tenant settings", description = "Update settings for the current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateCurrentSettings(
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Updating settings for current tenant");
        TenantSettingsResponse settings = settingsService.updateCurrentTenantSettings(request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update branding settings only
     */
    @PutMapping("/branding")
    @Operation(summary = "Update branding", description = "Update branding settings (logo, colors, name)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateBranding(
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Updating branding for current tenant");
        TenantSettingsResponse settings = settingsService.updateCurrentTenantSettings(request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update feature flags
     */
    @PutMapping("/features")
    @Operation(summary = "Update feature flags", description = "Enable/disable modules for the school")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateFeatures(
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Updating features for current tenant");
        TenantSettingsResponse settings = settingsService.updateCurrentTenantSettings(request);
        return ResponseEntity.ok(settings);
    }

    // ===== Super Admin Endpoints (for managing any tenant's settings) =====

    /**
     * Get settings for a specific tenant (Super Admin only)
     */
    @GetMapping("/{tenantId}")
    @Operation(summary = "Get tenant settings by ID", description = "Super Admin: Get settings for any tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> getSettingsByTenantId(@PathVariable UUID tenantId) {
        log.info("Super Admin fetching settings for tenant: {}", tenantId);
        TenantSettingsResponse settings = settingsService.getSettingsByTenantId(tenantId.toString());
        return ResponseEntity.ok(settings);
    }

    /**
     * Update settings for a specific tenant (Super Admin only)
     */
    @PutMapping("/{tenantId}")
    @Operation(summary = "Update tenant settings by ID", description = "Super Admin: Update settings for any tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateSettingsByTenantId(
            @PathVariable UUID tenantId,
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Super Admin updating settings for tenant: {}", tenantId);
        TenantSettingsResponse settings = settingsService.updateSettings(tenantId.toString(), request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update branding for a specific tenant (Super Admin only)
     */
    @PutMapping("/{tenantId}/branding")
    @Operation(summary = "Update branding by tenant ID", description = "Super Admin: Update branding for any tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateBrandingByTenantId(
            @PathVariable UUID tenantId,
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Super Admin updating branding for tenant: {}", tenantId);
        TenantSettingsResponse settings = settingsService.updateBranding(tenantId.toString(), request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update feature flags for a specific tenant (Super Admin only)
     */
    @PutMapping("/{tenantId}/features")
    @Operation(summary = "Update features by tenant ID", description = "Super Admin: Update features for any tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantSettingsResponse> updateFeaturesByTenantId(
            @PathVariable UUID tenantId,
            @Valid @RequestBody TenantSettingsRequest request) {
        log.info("Super Admin updating features for tenant: {}", tenantId);
        TenantSettingsResponse settings = settingsService.updateFeatureFlags(tenantId.toString(), request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Delete settings for a specific tenant (Super Admin only)
     */
    @DeleteMapping("/{tenantId}")
    @Operation(summary = "Delete tenant settings", description = "Super Admin: Delete settings for a tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteSettings(@PathVariable UUID tenantId) {
        log.info("Super Admin deleting settings for tenant: {}", tenantId);
        settingsService.deleteSettings(tenantId.toString());
        return ResponseEntity.ok(ApiResponse.success("Settings deleted successfully"));
    }
}
