package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.BulkTeacherAttendanceRequest;
import com.schoolmgmt.dto.response.BulkTeacherAttendanceResponse;
import com.schoolmgmt.dto.response.AttendanceResponse;
import com.schoolmgmt.service.BulkTeacherAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teachers/bulk-attendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bulk Teacher Attendance Management", description = "Bulk teacher attendance management APIs")
public class BulkTeacherAttendanceController {

    private final BulkTeacherAttendanceService bulkTeacherAttendanceService;

    @PostMapping
    @Operation(summary = "Mark attendance for multiple teachers", description = "Mark attendance for multiple teachers in bulk with validation and error reporting")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<BulkTeacherAttendanceResponse> markBulkTeacherAttendance(@Valid @RequestBody BulkTeacherAttendanceRequest request) {
        log.info("Starting bulk teacher attendance marking: {} teachers", String.valueOf(request.getAttendanceRecords().size()));
        BulkTeacherAttendanceResponse response = bulkTeacherAttendanceService.createBulkTeacherAttendance(request);
        
        if (response.getFailed() > 0) {
            log.warn("Bulk teacher attendance marking completed with {} successful and {} failed", 
                    response.getSuccessful(), response.getFailed());
        } else {
            log.info("Bulk teacher attendance marking completed successfully: {} records created", response.getSuccessful());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/template")
    @Operation(summary = "Get bulk teacher attendance template", description = "Get the required template structure for bulk teacher attendance marking")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<BulkTeacherAttendanceRequest> getBulkTeacherAttendanceTemplate() {
        log.info("Providing bulk teacher attendance template");
        BulkTeacherAttendanceRequest template = bulkTeacherAttendanceService.getBulkTeacherAttendanceTemplate();
        return ResponseEntity.ok(template);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate bulk teacher attendance data", description = "Validate teacher attendance data without creating records")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<BulkTeacherAttendanceResponse> validateBulkTeacherAttendance(@Valid @RequestBody BulkTeacherAttendanceRequest request) {
        log.info("Validating bulk teacher attendance data: {} teachers", String.valueOf(request.getAttendanceRecords().size()));
        BulkTeacherAttendanceResponse response = bulkTeacherAttendanceService.validateBulkTeacherAttendance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get teacher attendance records", description = "Get teacher attendance records with optional filtering")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER', 'TEACHER')")
    public ResponseEntity<List<AttendanceResponse>> getTeacherAttendanceRecords(
            @Parameter(description = "Filter by date") @RequestParam(required = false) LocalDate date,
            @Parameter(description = "Filter by teacher ID") @RequestParam(required = false) UUID teacherId,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department) {
        log.info("Getting teacher attendance records with filters - date: {}, teacherId: {}, department: {}", 
                date, teacherId, department);
        
        List<AttendanceResponse> records = bulkTeacherAttendanceService.getTeacherAttendanceRecords(date, teacherId, department);
        return ResponseEntity.ok(records);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update teacher attendance", description = "Update existing teacher attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<AttendanceResponse> updateTeacherAttendance(
            @Parameter(description = "Attendance record ID") @PathVariable UUID id,
            @Valid @RequestBody BulkTeacherAttendanceRequest.TeacherAttendanceRecord request) {
        log.info("Updating teacher attendance record: {}", id);
        try {
            AttendanceResponse updatedRecord = bulkTeacherAttendanceService.updateTeacherAttendance(id, request);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            log.error("Error updating teacher attendance {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete teacher attendance", description = "Delete teacher attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<Void> deleteTeacherAttendance(
            @Parameter(description = "Attendance record ID") @PathVariable UUID id) {
        log.info("Deleting teacher attendance record: {}", id);
        try {
            bulkTeacherAttendanceService.deleteTeacherAttendance(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting teacher attendance {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
