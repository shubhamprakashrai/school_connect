package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
import com.schoolmgmt.dto.request.CreateSectionRequest;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.service.ClassSectionService;
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
 * REST controller for class and section management operations.
 */
@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Class & Section Management", description = "APIs for managing school classes and sections")
public class ClassSectionController {
    
    private final ClassSectionService classSectionService;
    
    @PostMapping
    @Operation(summary = "Create new school class", description = "Create a new class with optional sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SchoolClassResponse> createSchoolClass(@Valid @RequestBody CreateSchoolClassRequest request) {
        log.info("Creating new school class: {}", request.getCode());
        SchoolClassResponse response = classSectionService.createSchoolClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all classes", description = "Get all classes with their sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<SchoolClassResponse>> getAllClasses(
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all classes");
        Page<SchoolClassResponse> classes = classSectionService.getAllClasses(pageable);
        return ResponseEntity.ok(classes);
    }
    
    @GetMapping("/{classId}")
    @Operation(summary = "Get class by ID", description = "Get class details with sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<SchoolClassResponse> getClassById(@PathVariable UUID classId) {
        log.info("Fetching class: {}", classId);
        SchoolClassResponse response = classSectionService.getClassById(classId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/sections")
    @Operation(summary = "Create new section", description = "Create a new section for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody CreateSectionRequest request) {
        log.info("Creating new section: {} for class: {}", request.getName(), request.getSchoolClassId());
        SectionResponse response = classSectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{classId}/sections")
    @Operation(summary = "Get sections by class", description = "Get all sections for a specific class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<SectionResponse>> getSectionsByClass(@PathVariable UUID classId) {
        log.info("Fetching sections for class: {}", classId);
        List<SectionResponse> sections = classSectionService.getSectionsByClassId(classId);
        return ResponseEntity.ok(sections);
    }
    
    @PostMapping("/{classId}/sections/bulk")
    @Operation(summary = "Create multiple sections", description = "Create multiple sections for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<SectionResponse>> createMultipleSections(
            @PathVariable UUID classId,
            @Valid @RequestBody List<CreateSectionRequest> requests) {
        log.info("Creating {} sections for class: {}", requests.size(), classId);
        
        // Set class ID for all requests
        requests.forEach(request -> request.setSchoolClassId(classId));
        
        List<SectionResponse> sections = classSectionService.createSectionsForClass(classId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(sections);
    }
    
    @PostMapping("/{classId}/sections/default")
    @Operation(summary = "Create default section", description = "Create default section 'A' for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<SectionResponse>> createDefaultSection(@PathVariable UUID classId) {
        log.info("Creating default section for class: {}", classId);
        List<SectionResponse> sections = classSectionService.createDefaultSection(classId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sections);
    }
}