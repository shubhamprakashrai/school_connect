package com.schoolmgmt.controller;


import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.dto.response.TenantStatistics;
import com.schoolmgmt.service.TenantServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "Tenant registration, activation, and resource management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

        private final TenantServiceInterface tenantService;

        /**
         * Register a new tenant (school)
         */
        @PostMapping("/register")
        @Operation(summary = "Register new tenant", description = "Register a new school tenant in the system")
        public ResponseEntity<TenantRegistrationResponse> registerTenant(
                @Valid @RequestBody TenantRegistrationRequest request
        )
        {
            log.info("Creating tenant: {} {}", request.getName(), request.getSubdomain(), request.getEmail(), request.getPhone());
            TenantRegistrationResponse response = tenantService.registerTenant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }


        /**
         * Get current tenant details based on TenantContext
         */
        @GetMapping("/current")
        @Operation(summary = "Get current tenant", description = "Get details of the current tenant based on the authenticated user's context")
        public ResponseEntity<TenantResponse> getCurrentTenant() {
            TenantResponse response = tenantService.getCurrentTenant();
            return ResponseEntity.ok(response);
        }

        /**
         * Update a tenant
         */
        @PutMapping("/{tenantId}")
        @Operation(summary = "Update tenant", description = "Update tenant information by tenant ID")
        public ResponseEntity<TenantResponse> updateTenant(
                @PathVariable UUID tenantId,
                @Valid @RequestBody UpdateTenantRequest request
        ) {
            TenantResponse response = tenantService.updateTenant(tenantId, request);
            return ResponseEntity.ok(response);
        }

        /**
         * Activate a tenant
         */
        @PostMapping("/{tenantId}/activate")
        @Operation(summary = "Activate tenant", description = "Activate a tenant to enable system access")
        public ResponseEntity<Void> activateTenant(@PathVariable UUID tenantId) {
            tenantService.activateTenant(tenantId);
            return ResponseEntity.ok().build();
        }

        /**
         * Suspend a tenant
         */
        @PostMapping("/{tenantId}/suspend")
        @Operation(summary = "Suspend tenant", description = "Suspend a tenant and restrict system access")
        public ResponseEntity<Void> suspendTenant(
                @PathVariable UUID tenantId,
                @RequestParam(required = false) String reason
        ) {
            tenantService.suspendTenant(tenantId, reason);
            return ResponseEntity.ok().build();
        }

        /**
         * Get tenant statistics
         */
        @GetMapping("/statistics")
        @Operation(summary = "Get tenant statistics", description = "Get resource usage statistics for the current tenant")
        public ResponseEntity<TenantStatistics> getTenantStatistics() {
            TenantStatistics stats = tenantService.getTenantStatistics();
            return ResponseEntity.ok(stats);
        }

        /**
         * Check if tenant can add a student
         */
        @GetMapping("/can-add-student")
        @Operation(summary = "Check student capacity", description = "Check if the tenant can add another student within its subscription limits")
        public ResponseEntity<Boolean> canAddStudent() {
            return ResponseEntity.ok(tenantService.canAddStudent());
        }

        /**
         * Check if tenant can add a teacher
         */
        @GetMapping("/can-add-teacher")
        @Operation(summary = "Check teacher capacity", description = "Check if the tenant can add another teacher within its subscription limits")
        public ResponseEntity<Boolean> canAddTeacher() {
            return ResponseEntity.ok(tenantService.canAddTeacher());
        }

        /**
         * Update student count
         */
        @PostMapping("/update-student-count")
        @Operation(summary = "Update student count", description = "Adjust the tenant's current student count by a delta value")
        public ResponseEntity<Void> updateStudentCount(@RequestParam int delta) {
            tenantService.updateStudentCount(delta);
            return ResponseEntity.ok().build();
        }

        /**
         * Update teacher count
         */
        @PostMapping("/update-teacher-count")
        @Operation(summary = "Update teacher count", description = "Adjust the tenant's current teacher count by a delta value")
        public ResponseEntity<Void> updateTeacherCount(@RequestParam int delta) {
            tenantService.updateTeacherCount(delta);
            return ResponseEntity.ok().build();
        }

        /**
         * Update storage usage
         */
        @PostMapping("/update-storage")
        @Operation(summary = "Update storage usage", description = "Adjust the tenant's current storage usage by a delta value in megabytes")
        public ResponseEntity<Void> updateStorageUsage(@RequestParam int delta) {
            tenantService.updateStorageUsage(delta);
            return ResponseEntity.ok().build();
        }
    }







