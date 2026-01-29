package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.CreateAssignmentRequest;
import com.schoolmgmt.dto.request.GradeSubmissionRequest;
import com.schoolmgmt.dto.request.SubmitAssignmentRequest;
import com.schoolmgmt.dto.request.UpdateAssignmentRequest;
import com.schoolmgmt.dto.response.AssignmentResponse;
import com.schoolmgmt.dto.response.AssignmentSubmissionResponse;
import com.schoolmgmt.model.Assignment;
import com.schoolmgmt.service.AssignmentService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Assignment Management", description = "APIs for managing assignments, submissions, and grading")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // ===== Assignment CRUD Endpoints =====

    @PostMapping
    @Operation(summary = "Create assignment", description = "Create a new assignment")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request) {
        log.info("Creating assignment: {}", request.getTitle());
        AssignmentResponse created = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get assignments", description = "Get paginated list of assignments with optional filters")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Page<AssignmentResponse>> getAssignments(
            @RequestParam(required = false) UUID classId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) Assignment.AssignmentStatus status,
            @RequestParam(required = false) Assignment.AssignmentType type,
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                assignmentService.getAssignments(classId, teacherId, subjectId, status, type, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID", description = "Get assignment details")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update assignment", description = "Update assignment details")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete assignment", description = "Delete an assignment")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get assignments by class", description = "Get all assignments for a class")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByClass(classId));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get assignments by teacher", description = "Get all assignments for a teacher")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByTeacher(teacherId));
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get assignments by subject", description = "Get all assignments for a subject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsBySubject(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySubject(subjectId));
    }

    // ===== Submission Endpoints =====

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit assignment", description = "Submit an assignment (student)")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAssignmentRequest request) {
        log.info("Submitting assignment: {} by student: {}", id, request.getStudentId());
        AssignmentSubmissionResponse response = assignmentService.submitAssignment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "Get submissions", description = "Get all submissions for an assignment")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissionsByAssignment(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentService.getSubmissionsByAssignment(id));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade submission", description = "Grade a student's submission")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentSubmissionResponse> gradeSubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        log.info("Grading submission: {}", submissionId);
        return ResponseEntity.ok(assignmentService.gradeSubmission(submissionId, request));
    }

    @GetMapping("/student/{studentId}/submissions")
    @Operation(summary = "Get student submissions", description = "Get all submissions by a student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissionsByStudent(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(assignmentService.getSubmissionsByStudent(studentId));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get assignment statistics", description = "Get statistics for an assignment")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAssignmentStatistics(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentService.getAssignmentStatistics(id));
    }
}
