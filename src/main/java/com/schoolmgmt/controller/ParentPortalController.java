package com.schoolmgmt.controller;

import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.service.ParentPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Parent Portal operations
 * Provides endpoints for parents to view their students' information
 */
@RestController
@RequestMapping("/parent-portal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parent Portal", description = "Parent Portal API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    /**
     * Get all students associated with the current parent
     * @return List of student responses
     */
    @GetMapping("/students")
    @Operation(summary = "Get all my students", description = "Get all students associated with the current parent (both children and wards)")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<StudentResponse>> getMyStudents() {
        log.info("API called: Get my students");
        
        List<StudentResponse> students = parentPortalService.getMyStudents();
        
        log.info("Returning {} students for parent", students.size());
        
        return ResponseEntity.ok(students);
    }

    /**
     * Get specific student details if the current parent has access
     * @param studentId Student ID
     * @return Student response
     */
    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get student details", description = "Get specific student details if the current parent has access")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<StudentResponse> getStudentDetails(
            @Parameter(description = "Student ID", required = true)
            @PathVariable UUID studentId) {
        
        log.info("API called: Get student details for student {}", studentId);
        
        StudentResponse student = parentPortalService.getStudentDetails(studentId);
        
        log.info("Returning details for student {} to parent", studentId);
        
        return ResponseEntity.ok(student);
    }

    /**
     * Check if current parent has access to a specific student
     * @param studentId Student ID
     * @return Boolean indicating access
     */
    @GetMapping("/students/{studentId}/access-check")
    @Operation(summary = "Check student access", description = "Check if current parent has access to a specific student")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Boolean> checkStudentAccess(
            @Parameter(description = "Student ID", required = true)
            @PathVariable UUID studentId) {
        
        log.info("API called: Check access to student {}", studentId);
        
        boolean hasAccess = parentPortalService.hasAccessToStudent(studentId);
        
        log.info("Access check for student {}: {}", studentId, hasAccess);
        
        return ResponseEntity.ok(hasAccess);
    }

    /**
     * Get current parent profile information
     * @return Parent information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get parent profile", description = "Get current parent's profile information")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<com.schoolmgmt.dto.response.ParentResponse> getParentProfile() {
        log.info("API called: Get parent profile");
        
        com.schoolmgmt.model.Parent parent = parentPortalService.getCurrentParent();
        
        com.schoolmgmt.dto.response.ParentResponse response = com.schoolmgmt.dto.response.ParentResponse.builder()
                .parentId(parent.getId())
                .firstname(parent.getFirstName())
                .middlename(parent.getMiddleName())
                .lastname(parent.getLastName())
                .email(parent.getEmail())
                .phone(parent.getPhone())
                .parentType(parent.getParentType().toString())
                .status(parent.getStatus().toString())
                .portalAccessEnabled(parent.getPortalAccessEnabled())
                .isPrimaryContact(parent.getIsPrimaryContact())
                .isEmergencyContact(parent.getIsEmergencyContact())
                .canPickupChild(parent.getCanPickupChild())
                .preferredLanguage(parent.getPreferredLanguage())
                .receiveSms(parent.getReceiveSms())
                .receiveEmail(parent.getReceiveEmail())
                .receiveAppNotifications(parent.getReceiveAppNotifications())
                .studentIds(parent.getAllStudents().stream()
                        .map(student -> student.getId())
                        .toList())
                .build();
        
        log.info("Returning profile for parent {}", parent.getId());
        
        return ResponseEntity.ok(response);
    }
}
