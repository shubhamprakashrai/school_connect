package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.BulkPromoteStudentsRequest;
import com.schoolmgmt.dto.request.SessionMigrationRequest;
import com.schoolmgmt.service.SessionMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for academic session migration and student promotion.
 */
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Session Migration", description = "Academic year migration and student promotion")
public class SessionMigrationController {

    private final SessionMigrationService sessionMigrationService;

    @PostMapping("/promote")
    @Operation(summary = "Promote students", description = "Promote a batch of students to new classes/sections")
    public ResponseEntity<ApiResponse> promoteStudents(@Valid @RequestBody BulkPromoteStudentsRequest request) {
        log.info("Received student promotion request for {} students", request.getStudentPromotions().size());
        try {
            Map<String, Object> summary = sessionMigrationService.promoteStudents(request.getStudentPromotions());
            return ResponseEntity.ok(ApiResponse.success("Student promotion completed", summary));
        } catch (Exception e) {
            log.error("Student promotion failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Student promotion failed: " + e.getMessage()));
        }
    }

    @PostMapping("/migrate")
    @Operation(summary = "Migrate session", description = "Create a new academic year, deactivate old assignments, and activate the new year")
    public ResponseEntity<ApiResponse> migrateSession(@Valid @RequestBody SessionMigrationRequest request) {
        log.info("Received session migration request for year: {}", request.getNewYearName());
        try {
            Map<String, Object> summary = sessionMigrationService.migrateSession(
                    request.getNewYearName(), request.getStartDate(), request.getEndDate());
            return ResponseEntity.ok(ApiResponse.success("Session migration completed successfully", summary));
        } catch (Exception e) {
            log.error("Session migration failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Session migration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/migration-status")
    @Operation(summary = "Get migration status", description = "Check current academic year info, student count, and active assignments")
    public ResponseEntity<ApiResponse> getMigrationStatus() {
        log.info("Fetching migration status");
        try {
            Map<String, Object> status = sessionMigrationService.getMigrationStatus();
            return ResponseEntity.ok(ApiResponse.success("Migration status retrieved", status));
        } catch (Exception e) {
            log.error("Failed to get migration status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to get migration status: " + e.getMessage()));
        }
    }
}
