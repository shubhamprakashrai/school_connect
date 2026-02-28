package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.BulkAttendanceRequest;
import com.schoolmgmt.dto.request.StudentAttendanceRequest;
import com.schoolmgmt.dto.response.AttendanceSummaryResponse;
import com.schoolmgmt.dto.response.StudentAttendanceResponse;
import com.schoolmgmt.service.StudentAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for student attendance management.
 * Attendance is validated against tenant calendar (working days only).
 */
@RestController
@RequestMapping("student/attendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Student Attendance", description = "Student attendance management APIs - validates against tenant calendar")
public class StudentAttendanceController {

    private final StudentAttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Mark single student attendance", description = "Mark attendance for a single student (validates against tenant calendar)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> markAttendance(
            @Valid @RequestBody StudentAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to mark attendance for student: {} on date: {}", request.getStudentId(), request.getAttendanceDate());
        // Extract user ID from authentication - simplified, adjust based on your UserDetails implementation
        UUID markedBy = extractUserId(userDetails);
        StudentAttendanceResponse response = attendanceService.markAttendance(request, markedBy);
        return new ResponseEntity<>(ApiResponse.success("Attendance marked successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Mark bulk attendance", description = "Mark attendance for multiple students in a section (validates against tenant calendar)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAttendanceResponse>>> markBulkAttendance(
            @Valid @RequestBody BulkAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to mark bulk attendance for section: {} on date: {} for {} students",
                request.getSectionId(), request.getAttendanceDate(), request.getAttendanceRecords().size());
        UUID markedBy = extractUserId(userDetails);
        List<StudentAttendanceResponse> responses = attendanceService.markBulkAttendance(request, markedBy);
        return new ResponseEntity<>(ApiResponse.success("Bulk attendance marked successfully", responses), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID", description = "Get attendance record by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> getAttendanceById(
            @Parameter(description = "Attendance ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to get attendance: {}", id);
        StudentAttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance fetched successfully", response));
    }

    @GetMapping("/student/{studentId}/date/{date}")
    @Operation(summary = "Get student attendance by date", description = "Get attendance for a specific student on a specific date")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> getAttendanceByStudentAndDate(
            @Parameter(description = "Student ID", required = true)
            @PathVariable UUID studentId,
            @Parameter(description = "Date (YYYY-MM-DD)", required = true, example = "2025-01-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to get attendance for student: {} on date: {}", studentId, date);
        StudentAttendanceResponse response = attendanceService.getAttendanceByStudentAndDate(studentId, date);
        return ResponseEntity.ok(ApiResponse.success("Attendance fetched successfully", response));
    }

    @GetMapping("/section/{sectionId}/date/{date}")
    @Operation(summary = "Get section attendance by date", description = "Get all attendance records for a section on a specific date")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAttendanceResponse>>> getAttendanceBySectionAndDate(
            @Parameter(description = "Section ID", required = true)
            @PathVariable UUID sectionId,
            @Parameter(description = "Date (YYYY-MM-DD)", required = true, example = "2025-01-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to get attendance for section: {} on date: {}", sectionId, date);
        List<StudentAttendanceResponse> responses = attendanceService.getAttendanceBySectionAndDate(sectionId, date);
        return ResponseEntity.ok(ApiResponse.success("Section attendance fetched successfully", responses));
    }

    @GetMapping("/student/{studentId}/summary")
    @Operation(summary = "Get student attendance summary", description = "Get attendance summary for a student in a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getStudentAttendanceSummary(
            @Parameter(description = "Student ID", required = true)
            @PathVariable UUID studentId,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2025-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to get attendance summary for student: {} from {} to {}", studentId, startDate, endDate);
        AttendanceSummaryResponse response = attendanceService.getStudentAttendanceSummary(studentId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Attendance summary fetched successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update attendance", description = "Update an existing attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> updateAttendance(
            @Parameter(description = "Attendance ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody StudentAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to update attendance: {}", id);
        UUID updatedBy = extractUserId(userDetails);
        StudentAttendanceResponse response = attendanceService.updateAttendance(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance", description = "Delete an attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(
            @Parameter(description = "Attendance ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to delete attendance: {}", id);
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance deleted successfully", null));
    }

    /**
     * Extracts user ID from authentication principal.
     * Adjust based on your UserDetails implementation.
     */
    private UUID extractUserId(UserDetails userDetails) {
        // This is a placeholder - implement based on your authentication setup
        // You might need to get the User from your UserRepository using username
        // For now, returning a placeholder UUID - replace with actual implementation
        if (userDetails == null) {
            return UUID.randomUUID(); // Should be replaced with actual logic
        }
        // Example: return userRepository.findByUsername(userDetails.getUsername()).map(User::getId).orElse(null);
        return UUID.randomUUID(); // Placeholder - implement based on your auth setup
    }
}
