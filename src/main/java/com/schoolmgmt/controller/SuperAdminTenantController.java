package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.TenantFilterRequest;
import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.dto.response.TenantStatistics;
import com.schoolmgmt.service.SuperAdminTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SuperAdmin controller for tenant management operations.
 * Only accessible to users with SUPER_ADMIN role.
 */
@RestController
@RequestMapping("/superadmin/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SuperAdmin Tenant Management", description = "SuperAdmin APIs for managing all tenants in the system")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminTenantController {

    private final SuperAdminTenantService superAdminTenantService;

    /**
     * Create a new tenant (school) as SuperAdmin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create new tenant", 
               description = "Create a new tenant (school) in the system. Only accessible to SuperAdmin.")
    public ResponseEntity<TenantRegistrationResponse> createTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {
        
        log.info("SuperAdmin creating tenant: {}", request.getName());
        TenantRegistrationResponse response = superAdminTenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tenants with filtering and pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all tenants", 
               description = "Retrieve all tenants with optional filtering and pagination")
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @Parameter(description = "Filter criteria") TenantFilterRequest filter,
            Pageable pageable) {
        
        Page<TenantResponse> tenants = superAdminTenantService.getAllTenants(filter, pageable);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get tenant by ID
     */
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenant by ID", 
               description = "Retrieve detailed information about a specific tenant")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID tenantId) {
        
        TenantResponse tenant = superAdminTenantService.getTenantById(tenantId);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Update tenant information
     */
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update tenant", 
               description = "Update tenant information including limits and configuration")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {
        
        log.info("SuperAdmin updating tenant: {}", tenantId);
        TenantResponse response = superAdminTenantService.updateTenant(tenantId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a tenant
     */
    @PostMapping("/{tenantId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activate tenant", 
               description = "Activate a tenant and enable access to the system")
    public ResponseEntity<TenantResponse> activateTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin activating tenant: {}", tenantId);
        TenantResponse response = superAdminTenantService.activateTenant(tenantId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend a tenant
     */
    @PostMapping("/{tenantId}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend tenant", 
               description = "Suspend a tenant and restrict access to the system")
    public ResponseEntity<TenantResponse> suspendTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin suspending tenant: {} with reason: {}", tenantId, reason);
        TenantResponse response = superAdminTenantService.suspendTenant(tenantId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a tenant (soft delete)
     */
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete tenant", 
               description = "Soft delete a tenant (can be restored later)")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin deleting tenant: {} with reason: {}", tenantId, reason);
        superAdminTenantService.deleteTenant(tenantId, reason);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently delete a tenant (hard delete)
     */
    @DeleteMapping("/{tenantId}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Permanently delete tenant", 
               description = "Permanently delete a tenant and all associated data (irreversible)")
    public ResponseEntity<Void> permanentlyDeleteTenant(@PathVariable UUID tenantId) {
        
        log.warn("SuperAdmin permanently deleting tenant: {}", tenantId);
        superAdminTenantService.permanentlyDeleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a deleted tenant
     */
    @PostMapping("/{tenantId}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Restore deleted tenant", 
               description = "Restore a soft-deleted tenant")
    public ResponseEntity<TenantResponse> restoreTenant(@PathVariable UUID tenantId) {
        
        log.info("SuperAdmin restoring tenant: {}", tenantId);
        TenantResponse response = superAdminTenantService.restoreTenant(tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update tenant subscription plan
     */
    @PutMapping("/{tenantId}/subscription")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update subscription plan", 
               description = "Update tenant's subscription plan and associated limits")
    public ResponseEntity<TenantResponse> updateSubscriptionPlan(
            @PathVariable UUID tenantId,
            @RequestParam String subscriptionPlan,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin updating subscription for tenant: {} to: {}", tenantId, subscriptionPlan);
        TenantResponse response = superAdminTenantService.updateSubscriptionPlan(tenantId, subscriptionPlan, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Update tenant limits manually
     */
    @PutMapping("/{tenantId}/limits")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update tenant limits", 
               description = "Manually update tenant limits (overrides subscription defaults)")
    public ResponseEntity<TenantResponse> updateTenantLimits(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) Integer maxStudents,
            @RequestParam(required = false) Integer maxTeachers,
            @RequestParam(required = false) Integer maxStorageGb) {
        
        log.info("SuperAdmin updating limits for tenant: {}", tenantId);
        TenantResponse response = superAdminTenantService.updateTenantLimits(
            tenantId, maxStudents, maxTeachers, maxStorageGb);
        return ResponseEntity.ok(response);
    }

    /**
     * Get global tenant statistics
     */
    @GetMapping("/statistics/global")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get global statistics", 
               description = "Get system-wide tenant statistics")
    public ResponseEntity<Map<String, Object>> getGlobalStatistics() {
        
        Map<String, Object> stats = superAdminTenantService.getGlobalTenantStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get tenants by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenants by status", 
               description = "Retrieve all tenants with a specific status")
    public ResponseEntity<List<TenantResponse>> getTenantsByStatus(
            @PathVariable String status) {
        
        List<TenantResponse> tenants = superAdminTenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Search tenants
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Search tenants", 
               description = "Search tenants by name, subdomain, or email")
    public ResponseEntity<List<TenantResponse>> searchTenants(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<TenantResponse> tenants = superAdminTenantService.searchTenants(query, limit);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get tenant usage analytics
     */
    @GetMapping("/{tenantId}/analytics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenant analytics", 
               description = "Get detailed usage analytics for a specific tenant")
    public ResponseEntity<Map<String, Object>> getTenantAnalytics(@PathVariable UUID tenantId) {
        
        Map<String, Object> analytics = superAdminTenantService.getTenantAnalytics(tenantId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Bulk activate tenants
     */
    @PostMapping("/bulk/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Bulk activate tenants", 
               description = "Activate multiple tenants at once")
    public ResponseEntity<Map<String, String>> bulkActivateTenants(
            @RequestBody List<UUID> tenantIds,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin bulk activating {} tenants", tenantIds.size());
        Map<String, String> results = superAdminTenantService.bulkActivateTenants(tenantIds, reason);
        return ResponseEntity.ok(results);
    }

    /**
     * Bulk suspend tenants
     */
    @PostMapping("/bulk/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Bulk suspend tenants", 
               description = "Suspend multiple tenants at once")
    public ResponseEntity<Map<String, String>> bulkSuspendTenants(
            @RequestBody List<UUID> tenantIds,
            @RequestParam(required = false) String reason) {
        
        log.info("SuperAdmin bulk suspending {} tenants", tenantIds.size());
        Map<String, String> results = superAdminTenantService.bulkSuspendTenants(tenantIds, reason);
        return ResponseEntity.ok(results);
    }

    /**
     * Get tenant configuration
     */
    @GetMapping("/{tenantId}/configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenant configuration", 
               description = "Get tenant-specific configuration settings")
    public ResponseEntity<Map<String, Object>> getTenantConfiguration(@PathVariable UUID tenantId) {
        
        Map<String, Object> config = superAdminTenantService.getTenantConfiguration(tenantId);
        return ResponseEntity.ok(config);
    }

    /**
     * Update tenant configuration
     */
    @PutMapping("/{tenantId}/configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update tenant configuration", 
               description = "Update tenant-specific configuration settings")
    public ResponseEntity<Void> updateTenantConfiguration(
            @PathVariable UUID tenantId,
            @RequestBody Map<String, Object> configuration) {
        
        log.info("SuperAdmin updating configuration for tenant: {}", tenantId);
        superAdminTenantService.updateTenantConfiguration(tenantId, configuration);
        return ResponseEntity.ok().build();
    }
}