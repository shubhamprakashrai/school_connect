package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
import com.schoolmgmt.dto.request.UpdateSchoolClassRequest;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.service.SchoolClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "School Class Management", description = "Handles class CRUD operations")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    /**
     * Create a new school class.
     */
    @PostMapping("/create")
    @Operation(summary = "Create new class")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SchoolClassResponse> createClass(
            @Valid @RequestBody CreateSchoolClassRequest request) {

        log.info("Creating new school class: {}", request.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(schoolClassService.createClass(request));
    }

    /**
     * Get paginated list of classes.
     */
    @GetMapping
    @Operation(summary = "List all classes with pagination")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','TEACHER')")
    public ResponseEntity<Page<SchoolClassResponse>> getAllClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(schoolClassService.getAllClasses(pageable));
    }

    /**
     * Get class by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','TEACHER')")
    public ResponseEntity<SchoolClassResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolClassService.getClassById(id));
    }

    /**
     * Update class details.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update class")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SchoolClassResponse> updateClass(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSchoolClassRequest request) {

        return ResponseEntity.ok(schoolClassService.updateClass(id, request));
    }

    /**
     * Delete a class.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete class")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable UUID id) {
        schoolClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}