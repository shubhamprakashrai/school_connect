package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.ConfigUpdateRequest;
import com.schoolmgmt.dto.response.MobileConfigResponse;
import com.schoolmgmt.dto.request.DeleteConfigRequest;
import com.schoolmgmt.service.MobileConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = {"/api/config", "/config"}, produces = "application/json")
@Tag(name = "Mobile Configuration", description = "APIs for mobile app configuration management")
public class MobileConfigController {

    private final MobileConfigService service;

    public MobileConfigController(MobileConfigService service) {
        this.service = service;
    }

    @GetMapping("/mobile")
    @Operation(summary = "Get mobile configuration", description = "Retrieve the current mobile app configuration for a school")
    public ResponseEntity<MobileConfigResponse> getMobileConfig(
            @RequestParam(required = false) String schoolId) {
        return ResponseEntity.ok(service.getConfig(schoolId));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update configuration", description = "Update a configuration value by scope and key (Admin or Super Admin)")
    public ResponseEntity<?> updateConfig(@Valid @RequestBody ConfigUpdateRequest request) {
        try {
            log.info("Updating config for schoolId: {}, scope: {}, key: {}", 
                    request.getSchoolId(), request.getScope(), request.getKey());
            
            service.updateConfig(request);
            return ResponseEntity.ok("Config updated successfully");
        } catch (Exception e) {
            log.error("Error updating config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error updating config: " + e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete configuration", description = "Delete a configuration entry by scope and key (Super Admin only)")
    public ResponseEntity<?> deleteConfig(@Valid @RequestBody DeleteConfigRequest request) {
        try {
            log.info("Deleting config for schoolId: {}, scope: {}, key: {}", 
                    request.getSchoolId(), request.getScope(), request.getKey());
            
            boolean deleted = service.deleteConfig(
                    request.getScope(), 
                    request.getKey(), 
                    request.getSchoolId()
            );
            
            if (deleted) {
                return ResponseEntity.ok("Config deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error deleting config: " + e.getMessage());
        }
    }
}
