package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.mapper.SectionMapper;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for class-teacher assignment and section management operations.
 */
@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Class Management", description = "Class-teacher assignment and section management APIs")
public class ClassController {

    private final ClassService classService;
    private final SectionMapper sectionMapper;

    @PostMapping("/assign-teacher")
    @Operation(summary = "Assign teacher to section", description = "Assign a teacher as class teacher to a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> assignTeacherToSection(
            @RequestParam UUID teacherId,
            @RequestParam UUID sectionId) {
        log.info("Assigning teacher {} to section {}", teacherId, sectionId);
        
        Section section = classService.assignTeacherToSection(teacherId, sectionId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Teacher assigned to section successfully", 
                        sectionMapper.toResponse(section, section.getSchoolClass())
                ));
    }

    @DeleteMapping("/remove-teacher/{sectionId}")
    @Operation(summary = "Remove teacher from section", description = "Remove class teacher assignment from a section")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> removeTeacherFromSection(@PathVariable UUID sectionId) {
        log.info("Removing teacher from section {}", sectionId);
        
        Section section = classService.removeTeacherFromSection(sectionId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Teacher removed from section successfully",
                sectionMapper.toResponse(section, section.getSchoolClass())
        ));
    }

    @GetMapping("/section/{sectionId}/students")
    @Operation(summary = "Get students in section", description = "Get all students in a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsInSection(@PathVariable UUID sectionId) {
        log.info("Fetching students for section {}", sectionId);
        
        List<StudentResponse> students = classService.getStudentsInSection(sectionId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/teacher/{teacherId}/sections")
    @Operation(summary = "Get sections for teacher", description = "Get all sections assigned to a specific teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<SectionResponse>> getSectionsForTeacher(@PathVariable UUID teacherId) {
        log.info("Fetching sections for teacher {}", teacherId);
        
        List<Section> sections = classService.getSectionsForTeacher(teacherId);
        List<SectionResponse> responses = sections.stream()
                .map(section -> sectionMapper.toResponse(section, section.getSchoolClass()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/teacher/{teacherId}/section/{sectionId}/verify")
    @Operation(summary = "Verify teacher assignment", description = "Check if a teacher is assigned to a specific section")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse> verifyTeacherAssignment(
            @PathVariable UUID teacherId,
            @PathVariable UUID sectionId) {
        log.info("Verifying teacher {} assignment to section {}", teacherId, sectionId);
        
        boolean isAssigned = classService.isTeacherAssignedToSection(teacherId, sectionId);
        
        return ResponseEntity.ok(ApiResponse.success(
                isAssigned ? "Teacher is assigned to this section" : "Teacher is not assigned to this section",
                isAssigned
        ));
    }
}
