package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.ClassTeacherAssignmentRequest;
import com.schoolmgmt.dto.response.ClassTeacherResponse;
import com.schoolmgmt.service.ClassTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing class teacher assignments.
 */
@RestController
@RequestMapping("/class-teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Teacher Management", description = "APIs for managing class teacher assignments")
public class ClassTeacherController {

    private final ClassTeacherService classTeacherService;

    /**
     * Assigns a teacher as class teacher for a section.
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER-ADMIN')")
    @Operation(summary = "Assign class teacher", description = "Assigns a teacher as the class teacher for a specific section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Class teacher assigned successfully",
                    content = @Content(schema = @Schema(implementation = ClassTeacherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or teacher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Business rule violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ClassTeacherResponse>> assignClassTeacher(
            @Valid @RequestBody ClassTeacherAssignmentRequest request) {
        try {
            log.info("REST request to assign class teacher: teacher {} to section {}",
                    request.getTeacherId(), request.getSectionId());
            ClassTeacherResponse response = classTeacherService.assignClassTeacher(request);
            log.info("Class teacher assigned successfully: teacher {} to section {}",
                    request.getTeacherId(), request.getSectionId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Class teacher assigned successfully", response));
        } catch (Exception e) {
            log.error("Error assigning class teacher: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Removes the class teacher assignment from a section.
     */
    @DeleteMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Remove class teacher", description = "Removes the class teacher assignment from a specific section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class teacher removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section not found or no class teacher assigned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> removeClassTeacher(
            @Parameter(description = "Section ID", required = true)
            @PathVariable UUID sectionId) {
        try {
            log.info("REST request to remove class teacher from section: {}", sectionId);
            classTeacherService.removeClassTeacher(sectionId);
            log.info("Class teacher removed successfully from section: {}", sectionId);

            return ResponseEntity.ok(ApiResponse.success("Class teacher removed successfully"));
        } catch (Exception e) {
            log.error("Error removing class teacher from section {}: {}", sectionId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the class teacher for a specific section.
     */
    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get class teacher by section", description = "Retrieves the class teacher assigned to a specific section")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class teacher retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClassTeacherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section not found or no class teacher assigned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ClassTeacherResponse>> getClassTeacherBySection(
            @Parameter(description = "Section ID", required = true)
            @PathVariable UUID sectionId) {
        try {
            log.debug("REST request to get class teacher for section: {}", sectionId);
            ClassTeacherResponse response = classTeacherService.getClassTeacherBySection(sectionId);
            log.debug("Class teacher retrieved successfully for section: {}", sectionId);

            return ResponseEntity.ok(ApiResponse.success("Class teacher retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error getting class teacher for section {}: {}", sectionId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all sections where a specific teacher is the class teacher.
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get sections by class teacher", description = "Retrieves all sections where a specific teacher is assigned as class teacher")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sections retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClassTeacherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<ClassTeacherResponse>>> getSectionsByClassTeacher(
            @Parameter(description = "Teacher ID", required = true)
            @PathVariable UUID teacherId) {
        try {
            log.debug("REST request to get sections for class teacher: {}", teacherId);
            List<ClassTeacherResponse> response = classTeacherService.getSectionsByClassTeacher(teacherId);
            log.debug("Found {} sections for teacher: {}", response.size(), teacherId);

            return ResponseEntity.ok(ApiResponse.success("Sections retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error getting sections for teacher {}: {}", teacherId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all class teacher assignments with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all class teachers", description = "Retrieves all class teacher assignments with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class teachers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClassTeacherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Page<ClassTeacherResponse>>> getAllClassTeachers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.debug("REST request to get all class teachers - page: {}, size: {}, sortBy: {}, sortDir: {}",
                    page, size, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ClassTeacherResponse> response = classTeacherService.getAllClassTeachers(pageable);
            log.debug("Retrieved {} class teacher assignments (page {} of {})",
                    response.getTotalElements(), response.getNumber(), response.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("Class teachers retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error getting all class teachers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Transfers class teacher from one section to another.
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Transfer class teacher", description = "Transfers class teacher from one section to another")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class teacher transferred successfully",
                    content = @Content(schema = @Schema(implementation = ClassTeacherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Source or target section not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Source section has no class teacher"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ClassTeacherResponse>> transferClassTeacher(
            @Parameter(description = "Source Section ID (where class teacher is currently assigned)", required = true)
            @RequestParam UUID fromSectionId,

            @Parameter(description = "Target Section ID (where class teacher will be assigned)", required = true)
            @RequestParam UUID toSectionId) {
        try {
            log.info("REST request to transfer class teacher from section {} to section {}",
                    fromSectionId, toSectionId);
            ClassTeacherResponse response = classTeacherService.transferClassTeacher(fromSectionId, toSectionId);
            log.info("Class teacher transferred successfully from {} to {}", fromSectionId, toSectionId);

            return ResponseEntity.ok(ApiResponse.success("Class teacher transferred successfully", response));
        } catch (Exception e) {
            log.error("Error transferring class teacher from {} to {}: {}",
                    fromSectionId, toSectionId, e.getMessage(), e);
            throw e;
        }
    }
}
