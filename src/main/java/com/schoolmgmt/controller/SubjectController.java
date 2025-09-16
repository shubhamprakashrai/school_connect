package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.SubjectCreationRequest;
import com.schoolmgmt.model.Subject;
import com.schoolmgmt.service.SubjectService;
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

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Subject Management", description = "APIs for managing academic subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @Operation(summary = "Create new subject", description = "Create a new academic subject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectCreationRequest request) {
        log.info("Creating new subject: {}", request.getName());
        Subject createdSubject = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple subjects", description = "Create multiple subjects at once")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Subject>> createMultipleSubjects(@Valid @RequestBody List<SubjectCreationRequest> requests) {
        log.info("Creating {} subjects", requests.size());
        List<Subject> createdSubjects = subjectService.createMultipleSubjects(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubjects);
    }

    @GetMapping
    @Operation(summary = "Get all subjects", description = "Get paginated list of all subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<Subject>> getAllSubjects(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all subjects");
        Page<Subject> subjects = subjectService.getAllSubjects(pageable);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID", description = "Get subject details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Subject> getSubjectById(@PathVariable UUID id) {
        log.info("Fetching subject: {}", id);
        Subject subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(subject);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get subject by code", description = "Get subject details by subject code")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Subject> getSubjectByCode(@PathVariable String code) {
        log.info("Fetching subject by code: {}", code);
        Subject subject = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(subject);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get subjects by class", description = "Get all subjects assigned to a specific class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<Subject>> getSubjectsByClass(@PathVariable UUID classId) {
        log.info("Fetching subjects for class: {}", classId);
        List<Subject> subjects = subjectService.getSubjectsByClass(classId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get subjects by teacher", description = "Get all subjects assigned to a specific teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<Subject>> getSubjectsByTeacher(@PathVariable UUID teacherId) {
        log.info("Fetching subjects for teacher: {}", teacherId);
        List<Subject> subjects = subjectService.getSubjectsByTeacher(teacherId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/search")
    @Operation(summary = "Search subjects", description = "Search subjects by name or code")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<Subject>> searchSubjects(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Searching subjects with query: {}", query);
        Page<Subject> subjects = subjectService.searchSubjects(query, pageable);
        return ResponseEntity.ok(subjects);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subject", description = "Update subject information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Subject> updateSubject(@PathVariable UUID id, @Valid @RequestBody SubjectCreationRequest request) {
        log.info("Updating subject: {}", id);
        Subject updatedSubject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(updatedSubject);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subject", description = "Soft delete subject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable UUID id) {
        log.info("Deleting subject: {}", id);
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{subjectId}/assign-to-class/{classId}")
    @Operation(summary = "Assign subject to class", description = "Assign subject to a specific class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> assignSubjectToClass(@PathVariable UUID subjectId, @PathVariable UUID classId) {
        log.info("Assigning subject {} to class {}", subjectId, classId);
        subjectService.assignSubjectToClass(subjectId, classId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subjectId}/assign-to-teacher/{teacherId}")
    @Operation(summary = "Assign subject to teacher", description = "Assign subject to a specific teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> assignSubjectToTeacher(@PathVariable UUID subjectId, @PathVariable UUID teacherId) {
        log.info("Assigning subject {} to teacher {}", subjectId, teacherId);
        subjectService.assignSubjectToTeacher(subjectId, teacherId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{subjectId}/remove-from-class/{classId}")
    @Operation(summary = "Remove subject from class", description = "Remove subject assignment from a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeSubjectFromClass(@PathVariable UUID subjectId, @PathVariable UUID classId) {
        log.info("Removing subject {} from class {}", subjectId, classId);
        subjectService.removeSubjectFromClass(subjectId, classId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{subjectId}/remove-from-teacher/{teacherId}")
    @Operation(summary = "Remove subject from teacher", description = "Remove subject assignment from a teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removeSubjectFromTeacher(@PathVariable UUID subjectId, @PathVariable UUID teacherId) {
        log.info("Removing subject {} from teacher {}", subjectId, teacherId);
        subjectService.removeSubjectFromTeacher(subjectId, teacherId);
        return ResponseEntity.noContent().build();
    }
}