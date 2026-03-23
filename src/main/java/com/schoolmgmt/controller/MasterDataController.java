package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.MasterDataRequest;
import com.schoolmgmt.dto.response.MasterDataResponse;
import com.schoolmgmt.service.MasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/master-data")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Master Data Management", description = "APIs for managing master/reference data")
public class MasterDataController {

    private final MasterDataService masterDataService;

    @PostMapping
    @Operation(summary = "Create master data entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> createEntry(@Valid @RequestBody MasterDataRequest request) {
        log.info("Creating master data entry: {} / {}", request.getCategory(), request.getValue());
        MasterDataResponse response = masterDataService.createEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Master data entry created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get master data by category")
    public ResponseEntity<ApiResponse> getByCategory(@RequestParam String category) {
        List<MasterDataResponse> entries = masterDataService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Master data retrieved successfully", entries));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all master data grouped by category")
    public ResponseEntity<ApiResponse> getAllForTenant() {
        Map<String, List<MasterDataResponse>> grouped = masterDataService.getAllForTenant();
        return ResponseEntity.ok(ApiResponse.success("All master data retrieved successfully", grouped));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get available master data categories")
    public ResponseEntity<ApiResponse> getCategories() {
        List<String> categories = masterDataService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update master data entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateEntry(@PathVariable UUID id,
                                                   @Valid @RequestBody MasterDataRequest request) {
        log.info("Updating master data entry: {}", id);
        MasterDataResponse response = masterDataService.updateEntry(id, request);
        return ResponseEntity.ok(ApiResponse.success("Master data entry updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete master data entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteEntry(@PathVariable UUID id) {
        log.info("Deleting master data entry: {}", id);
        masterDataService.deleteEntry(id);
        return ResponseEntity.ok(ApiResponse.success("Master data entry deleted successfully"));
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed default master data for current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> seedDefaults() {
        log.info("Seeding default master data");
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        masterDataService.seedDefaultData(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Default master data seeded successfully"));
    }
}
