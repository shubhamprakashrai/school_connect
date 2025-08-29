package com.schoolmgmt.controller;


import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.dto.response.TenantStatistics;
import com.schoolmgmt.service.TenantServiceInterface;
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
@Tag(name = "Tenant Management", description = "User management APIs")


    public class TenantController {

        private final TenantServiceInterface tenantService;

        /**
         * Register a new tenant (school)
         */
        @PostMapping("/register")
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
        public ResponseEntity<TenantResponse> getCurrentTenant() {
            TenantResponse response = tenantService.getCurrentTenant();
            return ResponseEntity.ok(response);
        }

        /**
         * Update a tenant
         */
        @PutMapping("/{tenantId}")
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
        public ResponseEntity<Void> activateTenant(@PathVariable UUID tenantId) {
            tenantService.activateTenant(tenantId);
            return ResponseEntity.ok().build();
        }

        /**
         * Suspend a tenant
         */
        @PostMapping("/{tenantId}/suspend")
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
        public ResponseEntity<TenantStatistics> getTenantStatistics() {
            TenantStatistics stats = tenantService.getTenantStatistics();
            return ResponseEntity.ok(stats);
        }

        /**
         * Check if tenant can add a student
         */
        @GetMapping("/can-add-student")
        public ResponseEntity<Boolean> canAddStudent() {
            return ResponseEntity.ok(tenantService.canAddStudent());
        }

        /**
         * Check if tenant can add a teacher
         */
        @GetMapping("/can-add-teacher")
        public ResponseEntity<Boolean> canAddTeacher() {
            return ResponseEntity.ok(tenantService.canAddTeacher());
        }

        /**
         * Update student count
         */
        @PostMapping("/update-student-count")
        public ResponseEntity<Void> updateStudentCount(@RequestParam int delta) {
            tenantService.updateStudentCount(delta);
            return ResponseEntity.ok().build();
        }

        /**
         * Update teacher count
         */
        @PostMapping("/update-teacher-count")
        public ResponseEntity<Void> updateTeacherCount(@RequestParam int delta) {
            tenantService.updateTeacherCount(delta);
            return ResponseEntity.ok().build();
        }

        /**
         * Update storage usage
         */
        @PostMapping("/update-storage")
        public ResponseEntity<Void> updateStorageUsage(@RequestParam int delta) {
            tenantService.updateStorageUsage(delta);
            return ResponseEntity.ok().build();
        }
    }







