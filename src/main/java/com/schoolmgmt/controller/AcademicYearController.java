package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.service.AcademicYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for academic year management operations.
 */
@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Academic Year", description = "Academic year management APIs")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @PostMapping
    @Operation(summary = "Create academic year", description = "Create a new academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> createAcademicYear(
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Creating academic year: {}", name);
        AcademicYear academicYear = academicYearService.createAcademicYear(name, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Academic year created successfully", academicYear));
    }

    @GetMapping
    @Operation(summary = "Get all academic years", description = "Get all academic years for the current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResponse> getAllAcademicYears() {
        log.info("Fetching all academic years");
        List<AcademicYear> academicYears = academicYearService.getAllAcademicYears();
        return ResponseEntity.ok(ApiResponse.success("Academic years retrieved successfully", academicYears));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academic year by ID", description = "Get academic year details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResponse> getAcademicYearById(@PathVariable UUID id) {
        log.info("Fetching academic year: {}", id);
        AcademicYear academicYear = academicYearService.getAcademicYearById(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year retrieved successfully", academicYear));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active academic year", description = "Get the currently active academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResponse> getActiveAcademicYear() {
        log.info("Fetching active academic year");
        AcademicYear academicYear = academicYearService.getActiveAcademicYear();
        return ResponseEntity.ok(ApiResponse.success("Active academic year retrieved successfully", academicYear));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate academic year", description = "Activate an academic year (deactivates the current active one)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> activateAcademicYear(@PathVariable UUID id) {
        log.info("Activating academic year: {}", id);
        AcademicYear academicYear = academicYearService.activateAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year activated successfully", academicYear));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate academic year", description = "Deactivate an academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deactivateAcademicYear(@PathVariable UUID id) {
        log.info("Deactivating academic year: {}", id);
        AcademicYear academicYear = academicYearService.deactivateAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year deactivated successfully", academicYear));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete academic year", description = "Soft delete an academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteAcademicYear(@PathVariable UUID id) {
        log.info("Deleting academic year: {}", id);
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year deleted successfully"));
    }
}
