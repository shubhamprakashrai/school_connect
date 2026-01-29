package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.*;
import com.schoolmgmt.dto.response.*;
import com.schoolmgmt.dto.common.*;
import com.schoolmgmt.service.StudentBulkService;
import com.schoolmgmt.service.StudentService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for student management operations.
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Management", description = "Student management APIs")
public class StudentController {

    private final StudentService studentService;
    private final StudentBulkService studentBulkService;

    @PostMapping
    @Operation(summary = "Create new student", description = "Register a new student in the system")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        log.info("Creating new student: {} {}", request.getFirstName(), request.getLastName());
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "Update student", description = "Update student information")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable UUID studentId,
            @Valid @RequestBody UpdateStudentRequest request) {
        log.info("Updating student: {}", studentId);
        StudentResponse response = studentService.updateStudent(studentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get student by ID", description = "Get student details by student ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable UUID studentId) {
        log.info("Fetching student: {}", studentId);
        StudentResponse response = studentService.getStudentById(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all students", description = "Get all students with filtering and pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @ModelAttribute StudentFilterRequest filter,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching students with filter: {}", filter);
        Page<StudentResponse> students = studentService.getAllStudents(filter, pageable);
        return ResponseEntity.ok(students);
    }

    @PatchMapping("/{studentId}/status")
    @Operation(summary = "Update student status", description = "Update student status (ACTIVE, INACTIVE, etc.)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateStudentStatus(
            @PathVariable UUID studentId,
            @RequestParam String status) {
        log.info("Updating student status: {} to {}", studentId, status);
        studentService.updateStudentStatus(studentId, status);
        return ResponseEntity.ok(ApiResponse.success("Student status updated successfully"));
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Delete student", description = "Soft delete a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable UUID studentId) {
        log.info("Deleting student: {}", studentId);
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get student statistics", description = "Get statistics about students in the tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StudentStatistics> getStudentStatistics() {
        log.info("Fetching student statistics");
        StudentStatistics statistics = studentService.getStudentStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get students by class", description = "Get all students in a specific class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<StudentResponse>> getStudentsByClass(
            @PathVariable String classId,
            @RequestParam(required = false) String sectionId,
            @PageableDefault(size = 50, sort = "rollNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching students for class: {} section: {}", classId, sectionId);
        
        StudentFilterRequest filter = StudentFilterRequest.builder()
                .classId(classId)
                .sectionId(sectionId)
                .status("ACTIVE")
                .build();
        
        Page<StudentResponse> students = studentService.getAllStudents(filter, pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search")
    @Operation(summary = "Search students", description = "Search students by name or roll number")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<StudentResponse>> searchStudents(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Searching students with query: {}", query);

        StudentFilterRequest filter = StudentFilterRequest.builder()
                .search(query)
                .build();

        Page<StudentResponse> students = studentService.getAllStudents(filter, pageable);
        return ResponseEntity.ok(students);
    }

    // ==================== Bulk Operations ====================

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import students from CSV", description = "Bulk import students from a CSV file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BulkImportResult> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "classId", required = false) String classId) {
        log.info("Importing students from CSV, classId: {}", classId);
        BulkImportResult result = studentBulkService.importStudentsFromCsv(file, classId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export students to CSV", description = "Export students to a CSV file with optional filters")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(value = "classId", required = false) String classId,
            @RequestParam(value = "sectionId", required = false) String sectionId,
            @RequestParam(value = "status", required = false) String status) {
        log.info("Exporting students to CSV, classId: {}, sectionId: {}, status: {}", classId, sectionId, status);
        byte[] csvData = studentBulkService.exportStudentsToCsv(classId, sectionId, status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "students_export.csv");
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).body(csvData);
    }

    @GetMapping(value = "/import/template", produces = "text/csv")
    @Operation(summary = "Download import template", description = "Download an empty CSV template for student import")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        log.info("Downloading CSV import template");
        byte[] templateData = studentBulkService.generateImportTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "student_import_template.csv");
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).body(templateData);
    }
}
