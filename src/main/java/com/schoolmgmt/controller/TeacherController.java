package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.model.TeacherClass;
import com.schoolmgmt.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Teacher> createTeacher(@Valid @RequestBody TeacherCreationRequest request) {
        Teacher createdTeacher = teacherService.createTeacher(request);
        return new ResponseEntity<>(createdTeacher, HttpStatus.CREATED);
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherClass> assignTeacherToClass(@Valid @RequestBody TeacherAssignmentRequest request) {
        TeacherClass assignment = teacherService.assignTeacherToClass(request);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    // Other endpoints for GET, PUT, DELETE teachers would go here.
}