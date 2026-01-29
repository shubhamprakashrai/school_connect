package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.PlatformConfigRequest;
import com.schoolmgmt.dto.response.PlatformConfigResponse;
import com.schoolmgmt.model.User;
import com.schoolmgmt.service.PlatformConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for platform configuration management.
 * Most endpoints are restricted to Super Admin.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platform Config", description = "Platform configuration management APIs")
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    /**
     * Get mobile app configuration (PUBLIC - no auth required)
     */
    @GetMapping("/config/mobile")
    @Operation(summary = "Get mobile config", description = "Get configuration for mobile app (public endpoint)")
    public ResponseEntity<ApiResponse> getMobileConfig() {
        Map<String, Object> config = platformConfigService.getMobileConfig();
        return ResponseEntity.ok(ApiResponse.success("Mobile config retrieved successfully", config));
    }

    /**
     * Get full platform configuration (Super Admin only)
     */
    @GetMapping("/superadmin/platform/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get platform config", description = "Get full platform configuration (Super Admin only)")
    public ResponseEntity<ApiResponse> getConfig() {
        PlatformConfigResponse config = platformConfigService.getConfig();
        return ResponseEntity.ok(ApiResponse.success("Platform config retrieved successfully", config));
    }

    /**
     * Update platform configuration (Super Admin only)
     */
    @PutMapping("/superadmin/platform/config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update platform config", description = "Update platform configuration (Super Admin only)")
    public ResponseEntity<ApiResponse> updateConfig(
            @AuthenticationPrincipal User user,
            @RequestBody PlatformConfigRequest request) {

        log.info("Super Admin {} updating platform config", user.getUserId());
        PlatformConfigResponse config = platformConfigService.updateConfig(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Platform config updated successfully", config));
    }

    /**
     * Get feature flags (Super Admin only)
     */
    @GetMapping("/superadmin/platform/features")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get feature flags", description = "Get platform feature flags (Super Admin only)")
    public ResponseEntity<ApiResponse> getFeatureFlags() {
        PlatformConfigResponse config = platformConfigService.getConfig();
        return ResponseEntity.ok(ApiResponse.success("Feature flags retrieved successfully", config.getFeatureFlags()));
    }

    /**
     * Update feature flags (Super Admin only)
     */
    @PutMapping("/superadmin/platform/features")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update feature flags", description = "Update platform feature flags (Super Admin only)")
    public ResponseEntity<ApiResponse> updateFeatureFlags(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Boolean> featureFlags) {

        log.info("Super Admin {} updating feature flags", user.getUserId());
        PlatformConfigResponse config = platformConfigService.updateFeatureFlags(featureFlags, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Feature flags updated successfully", config));
    }

    /**
     * Update branding (Super Admin only)
     */
    @PutMapping("/superadmin/platform/branding")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update branding", description = "Update platform branding (Super Admin only)")
    public ResponseEntity<ApiResponse> updateBranding(
            @AuthenticationPrincipal User user,
            @RequestBody PlatformConfigRequest request) {

        log.info("Super Admin {} updating branding", user.getUserId());
        PlatformConfigResponse config = platformConfigService.updateBranding(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Branding updated successfully", config));
    }

    /**
     * Set maintenance mode (Super Admin only)
     */
    @PostMapping("/superadmin/platform/maintenance")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set maintenance mode", description = "Enable/disable maintenance mode (Super Admin only)")
    public ResponseEntity<ApiResponse> setMaintenanceMode(
            @AuthenticationPrincipal User user,
            @RequestParam boolean enabled,
            @RequestParam(required = false) String message) {

        log.info("Super Admin {} setting maintenance mode to {}", user.getUserId(), enabled);
        PlatformConfigResponse config = platformConfigService.setMaintenanceMode(enabled, message, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode " + (enabled ? "enabled" : "disabled"), config));
    }
}
