package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.model.TeacherClass;
import com.schoolmgmt.service.TeacherService;
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
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Teacher Management", description = "APIs for managing school teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @Operation(summary = "Create new teacher", description = "Create a new teacher with user account")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Teacher> createTeacher(@Valid @RequestBody TeacherCreationRequest request) {
        log.info("Creating new teacher: {}", request.getEmail());
        Teacher createdTeacher = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher);
    }

    @GetMapping
    @Operation(summary = "Get all teachers", description = "Get paginated list of all teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<Teacher>> getAllTeachers(
            @PageableDefault(size = 20, sort = "employeeId", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all teachers");
        Page<Teacher> teachers = teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID", description = "Get teacher details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable UUID id) {
        log.info("Fetching teacher: {}", id);
        Teacher teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get teacher by employee ID", description = "Get teacher details by employee ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Teacher> getTeacherByEmployeeId(@PathVariable String employeeId) {
        log.info("Fetching teacher by employee ID: {}", employeeId);
        Teacher teacher = teacherService.getTeacherByEmployeeId(employeeId);
        return ResponseEntity.ok(teacher);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update teacher", description = "Update teacher information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable UUID id, @Valid @RequestBody TeacherCreationRequest request) {
        log.info("Updating teacher: {}", id);
        Teacher updatedTeacher = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(updatedTeacher);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete teacher", description = "Soft delete teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable UUID id) {
        log.info("Deleting teacher: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments")
    @Operation(summary = "Assign teacher to class", description = "Assign teacher to class and subject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TeacherClass> assignTeacherToClass(@Valid @RequestBody TeacherAssignmentRequest request) {
        log.info("Assigning teacher to class");
        TeacherClass assignment = teacherService.assignTeacherToClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/{id}/assignments")
    @Operation(summary = "Get teacher assignments", description = "Get all class assignments for a teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherClass>> getTeacherAssignments(@PathVariable UUID id) {
        log.info("Fetching assignments for teacher: {}", id);
        List<TeacherClass> assignments = teacherService.getTeacherAssignments(id);
        return ResponseEntity.ok(assignments);
    }
}