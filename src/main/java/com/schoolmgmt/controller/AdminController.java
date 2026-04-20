package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.AdminRequest;
import com.schoolmgmt.dto.request.AdminUpdateRequest;
import com.schoolmgmt.dto.response.AdminResponse;
import com.schoolmgmt.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Admin management operations.
 * Provides CRUD endpoints for admin profiles with proper security and multi-tenant support.
 */
@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Management", description = "Admin management APIs")
public class AdminController {

    private final AdminService adminService;

    /**
     * Create a new admin.
     * Only users with ADMIN or SUPER_ADMIN role can create admins.
     *
     * @param request Admin creation request
     * @return ApiResponse with created admin details
     */
    @PostMapping
    @Operation(summary = "Create a new admin", description = "Create a new admin with user account and profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody AdminRequest request) {
        log.info("POST /admins - Creating new admin with employeeId: {}", request.getEmployeeId());
        AdminResponse admin = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin created successfully", admin));
    }

    /**
     * Get admin by ID.
     * Admins can view their own profile. ADMIN and SUPER_ADMIN can view any admin.
     *
     * @param adminId Admin ID
     * @return ApiResponse with admin details
     */
    @GetMapping("/{adminId}")
    @Operation(summary = "Get admin by ID", description = "Get admin details by admin ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(
            @Parameter(description = "Admin ID") @PathVariable UUID adminId) {
        log.info("GET /admins/{} - Fetching admin by ID", adminId);
        AdminResponse admin = adminService.getAdminById(adminId);
        return ResponseEntity.ok(ApiResponse.success(admin));
    }

    /**
     * Get all admins with pagination and filtering.
     * Only users with ADMIN or SUPER_ADMIN role can view all admins.
     *
     * @param status Filter by status (optional)
     * @param department Filter by department (optional)
     * @param designation Filter by designation (optional)
     * @param pageable Pagination information
     * @return Page of admin responses
     */
    @GetMapping
    @Operation(summary = "Get all admins", description = "Get all admins with pagination and filtering")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AdminResponse>> getAllAdmins(
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, SUSPENDED, TERMINATED)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by department") 
            @RequestParam(required = false) String department,
            @Parameter(description = "Filter by designation") 
            @RequestParam(required = false) String designation,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /admins - Fetching admins with filters - status: {}, department: {}, designation: {}",
                status, department, designation);
        Page<AdminResponse> admins = adminService.getAllAdmins(status, department, designation, pageable);
        return ResponseEntity.ok(admins);
    }

    /**
     * Update admin information.
     * Admins can update their own profile. ADMIN and SUPER_ADMIN can update any admin.
     *
     * @param adminId Admin ID
     * @param request Update request
     * @return ApiResponse with updated admin details
     */
    @PutMapping("/{adminId}")
    @Operation(summary = "Update admin", description = "Update admin information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @Parameter(description = "Admin ID") @PathVariable UUID adminId,
            @Valid @RequestBody AdminUpdateRequest request) {
        log.info("PUT /admins/{} - Updating admin", adminId);
        AdminResponse admin = adminService.updateAdmin(adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Admin updated successfully", admin));
    }

    /**
     * Delete admin.
     * Only users with ADMIN or SUPER_ADMIN role can delete admins.
     *
     * @param adminId Admin ID
     * @return ApiResponse confirming deletion
     */
    @DeleteMapping("/{adminId}")
    @Operation(summary = "Delete admin", description = "Delete admin (soft delete)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(
            @Parameter(description = "Admin ID") @PathVariable UUID adminId) {
        log.info("DELETE /admins/{} - Deleting admin", adminId);
        adminService.deleteAdmin(adminId);
        return ResponseEntity.ok(ApiResponse.success("Admin deleted successfully"));
    }
}
