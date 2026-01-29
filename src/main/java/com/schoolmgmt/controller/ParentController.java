package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.CreateParentRequest;
import com.schoolmgmt.dto.request.ParentFilterRequest;
import com.schoolmgmt.dto.request.UpdateParentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for parent/guardian management operations.
 */
@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Parent Management", description = "Parent/guardian management APIs")
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    @Operation(summary = "Create new parent", description = "Register a new parent/guardian in the system")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParentResponse> createParent(@Valid @RequestBody CreateParentRequest request) {
        log.info("Creating new parent: {} {}", request.getFirstName(), request.getLastName());
        ParentResponse response = parentService.createParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{parentId}")
    @Operation(summary = "Update parent", description = "Update parent/guardian information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParentResponse> updateParent(
            @PathVariable UUID parentId,
            @Valid @RequestBody UpdateParentRequest request) {
        log.info("Updating parent: {}", parentId);
        ParentResponse response = parentService.updateParent(parentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{parentId}")
    @Operation(summary = "Get parent by ID", description = "Get parent/guardian details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<ParentResponse> getParentById(@PathVariable UUID parentId) {
        log.info("Fetching parent: {}", parentId);
        ParentResponse response = parentService.getParentById(parentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all parents", description = "Get all parents with filtering and pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<ParentResponse>> getAllParents(
            @ModelAttribute ParentFilterRequest filter,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching parents with filter: {}", filter);
        Page<ParentResponse> parents = parentService.getAllParents(filter, pageable);
        return ResponseEntity.ok(parents);
    }

    @PatchMapping("/{parentId}/status")
    @Operation(summary = "Update parent status", description = "Update parent status (ACTIVE, INACTIVE, etc.)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateParentStatus(
            @PathVariable UUID parentId,
            @RequestParam String status) {
        log.info("Updating parent status: {} to {}", parentId, status);
        parentService.updateParentStatus(parentId, status);
        return ResponseEntity.ok(ApiResponse.success("Parent status updated successfully"));
    }

    @DeleteMapping("/{parentId}")
    @Operation(summary = "Delete parent", description = "Soft delete a parent/guardian")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteParent(@PathVariable UUID parentId) {
        log.info("Deleting parent: {}", parentId);
        parentService.deleteParent(parentId);
        return ResponseEntity.ok(ApiResponse.success("Parent deleted successfully"));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get parents by student", description = "Get all parents/guardians of a specific student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<ParentResponse>> getParentsByStudent(@PathVariable UUID studentId) {
        log.info("Fetching parents for student: {}", studentId);
        List<ParentResponse> parents = parentService.getParentsByStudentId(studentId);
        return ResponseEntity.ok(parents);
    }

    @PostMapping("/{parentId}/students/{studentId}")
    @Operation(summary = "Link parent to student", description = "Link a parent/guardian to a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParentResponse> linkParentToStudent(
            @PathVariable UUID parentId,
            @PathVariable UUID studentId) {
        log.info("Linking parent {} to student {}", parentId, studentId);
        ParentResponse response = parentService.linkParentToStudent(parentId, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search parents", description = "Search parents by name, email, or phone")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<ParentResponse>> searchParents(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Searching parents with query: {}", query);

        ParentFilterRequest filter = ParentFilterRequest.builder()
                .search(query)
                .build();

        Page<ParentResponse> parents = parentService.getAllParents(filter, pageable);
        return ResponseEntity.ok(parents);
    }
}
