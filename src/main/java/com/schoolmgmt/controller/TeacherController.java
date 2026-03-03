package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherUpdateRequest;
import com.schoolmgmt.dto.response.TeacherResponse;
import com.schoolmgmt.model.Teacher;
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


import java.util.UUID;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")

@Tag(name = "Teacher Management", description = "Teacher management APIs")

public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherCreationRequest request) {
        TeacherResponse createdTeacher = teacherService.createTeacher(request);
        return new ResponseEntity<>(createdTeacher, HttpStatus.CREATED);
    }

    @GetMapping("/{teacherId}")
    @Operation(summary = "Get teacher by ID", description = "Get teacher details by teacher ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','TEACHER')")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable UUID teacherId) {
        log.info("Fetching teacher: {}", teacherId);
        TeacherResponse teacher = teacherService.getTeacherById(teacherId);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping
    @Operation(summary = "Get all teachers", description = "Get all teachers with pagination and filtering")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','TEACHER')")
    public ResponseEntity<Page<TeacherResponse>> getAllTeachers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching teachers with filters - status: {}, department: {}", status, department);
        Page<TeacherResponse> teachers = teacherService.getAllTeachers(status, department, pageable);
        return ResponseEntity.ok(teachers);
    }

    @PutMapping("/{teacherId}")
    @Operation(summary = "Update teacher", description = "Update teacher information")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable UUID teacherId,
            @Valid @RequestBody TeacherUpdateRequest request) {
        log.info("Updating teacher: {}", teacherId);
        TeacherResponse updatedTeacher = teacherService.updateTeacher(teacherId, request);
        return ResponseEntity.ok(updatedTeacher);
    }

    @DeleteMapping("/{teacherId}")
    @Operation(summary = "Delete teacher", description = "Delete teacher and all associated assignments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable UUID teacherId) {
        log.info("Deleting teacher: {}", teacherId);
        teacherService.deleteTeacher(teacherId);
        return ResponseEntity.noContent().build();

    }
}