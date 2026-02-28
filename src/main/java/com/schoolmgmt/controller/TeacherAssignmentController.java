package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentUpdateRequest;
import com.schoolmgmt.dto.request.TeacherBatchAssignmentRequest;
import com.schoolmgmt.dto.response.TeacherAssignmentResponse;
import com.schoolmgmt.dto.response.TeacherSubjectInfoResponse;
import com.schoolmgmt.service.TeacherAssignmentService;
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
import java.util.UUID;

/**
 * REST controller for teacher assignment management operations.
 * Handles all assignment-related endpoints separately from teacher CRUD.
 */
@RestController
@RequestMapping("/teacher-assignments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Teacher Assignment Management", description = "Teacher assignment management APIs")
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    @PostMapping
    @Operation(summary = "Create teacher assignment", description = "Assign a teacher to a section and subject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TeacherAssignmentResponse> createAssignment(@Valid @RequestBody TeacherAssignmentRequest request) {
        log.info("Creating teacher assignment - Teacher: {}, Section: {}, Subject: {}", 
                request.getTeacherId(), request.getSectionId(), request.getSubjectId());
        
        TeacherAssignmentResponse response = teacherAssignmentService.createAssignment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{assignmentId}")
    @Operation(summary = "Get assignment by ID", description = "Get teacher assignment details by assignment ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherAssignmentResponse> getAssignmentById(@PathVariable UUID assignmentId) {
        log.info("Fetching assignment: {}", assignmentId);
        TeacherAssignmentResponse response = teacherAssignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get assignments by teacher", description = "Get all assignments for a specific teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherAssignmentResponse>> getAssignmentsByTeacher(@PathVariable UUID teacherId) {
        log.info("Fetching assignments for teacher: {}", teacherId);
        List<TeacherAssignmentResponse> assignments = teacherAssignmentService.getAssignmentsByTeacher(teacherId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get assignments by section", description = "Get all assignments for a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherAssignmentResponse>> getAssignmentsBySection(@PathVariable UUID sectionId) {
        log.info("Fetching assignments for section: {}", sectionId);
        List<TeacherAssignmentResponse> assignments = teacherAssignmentService.getAssignmentsBySection(sectionId);
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{assignmentId}")
    @Operation(summary = "Update assignment", description = "Update teacher assignment status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TeacherAssignmentResponse> updateAssignment(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody TeacherAssignmentUpdateRequest request) {
        log.info("Updating assignment: {}", assignmentId);
        TeacherAssignmentResponse response = teacherAssignmentService.updateAssignment(assignmentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{assignmentId}")
    @Operation(summary = "Delete assignment", description = "Delete teacher assignment")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID assignmentId) {
        log.info("Deleting assignment: {}", assignmentId);
        teacherAssignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch create assignments", description = "Assign a teacher to multiple subjects in a section at once")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TeacherAssignmentResponse>> createBatchAssignments(
            @Valid @RequestBody TeacherBatchAssignmentRequest request) {
        log.info("Creating batch assignments - Teacher: {}, Section: {}, Subjects count: {}",
                request.getTeacherId(), request.getSectionId(), request.getSubjectIds().size());

        List<TeacherAssignmentResponse> responses = teacherAssignmentService.createBatchAssignments(request);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/teacher/{teacherId}/subjects-info")
    @Operation(summary = "Get teacher subject info", description = "Get subject count and details for teacher profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherSubjectInfoResponse> getTeacherSubjectInfo(@PathVariable UUID teacherId) {
        log.info("Fetching subject info for teacher: {}", teacherId);
        TeacherSubjectInfoResponse response = teacherAssignmentService.getTeacherSubjectInfo(teacherId);
        return ResponseEntity.ok(response);
    }
}
