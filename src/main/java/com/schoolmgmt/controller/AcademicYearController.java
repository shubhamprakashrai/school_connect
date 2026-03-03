package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.AcademicYearRequest;
import com.schoolmgmt.dto.response.AcademicYearResponse;
import com.schoolmgmt.service.AcademicYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for academic year management.
 */
@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
//@Slf4j  // Not working, using manual logger
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Academic Year Management", description = "APIs for managing academic years/sessions")
public class AcademicYearController {

    private static final Logger log = LoggerFactory.getLogger(AcademicYearController.class);

    private final AcademicYearService academicYearService;

    @PostMapping
    @Operation(summary = "Create academic year", description = "Create a new academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> createAcademicYear(
            @Valid @RequestBody AcademicYearRequest request) {
        log.info("REST request to create academic year: {}", request.getName());
        AcademicYearResponse response = academicYearService.createAcademicYear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Academic year created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all academic years", description = "Get all academic years for current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAllAcademicYears() {
        log.debug("REST request to get all academic years");
        List<AcademicYearResponse> response = academicYearService.getAllAcademicYears();
        return ResponseEntity.ok(ApiResponse.success("Academic years retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academic year by ID", description = "Get academic year details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getAcademicYearById(
            @Parameter(description = "Academic Year ID", required = true)
            @PathVariable UUID id) {
        log.debug("REST request to get academic year: {}", id);
        AcademicYearResponse response = academicYearService.getAcademicYearById(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year retrieved successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active academic year", description = "Get the currently active academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getActiveAcademicYear() {
        log.debug("REST request to get active academic year");
        AcademicYearResponse response = academicYearService.getActiveAcademicYear();
        return ResponseEntity.ok(ApiResponse.success("Active academic year retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update academic year", description = "Update an existing academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> updateAcademicYear(
            @Parameter(description = "Academic Year ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AcademicYearRequest request) {
        log.info("REST request to update academic year: {}", id);
        AcademicYearResponse response = academicYearService.updateAcademicYear(id, request);
        return ResponseEntity.ok(ApiResponse.success("Academic year updated successfully", response));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Set active academic year", description = "Set an academic year as active (deactivates others)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> setActiveAcademicYear(
            @Parameter(description = "Academic Year ID to activate", required = true)
            @PathVariable UUID id) {
        log.info("REST request to set active academic year: {}", id);
        AcademicYearResponse response = academicYearService.setActiveAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year set as active successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete academic year", description = "Delete an academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicYear(
            @Parameter(description = "Academic Year ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to delete academic year: {}", id);
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success("Academic year deleted successfully"));
    }
}
