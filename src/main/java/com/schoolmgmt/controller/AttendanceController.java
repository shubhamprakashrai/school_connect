package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.AttendanceMarkingRequest;
import com.schoolmgmt.model.Attendance;
import com.schoolmgmt.service.AttendanceService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance Management", description = "APIs for managing student attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Mark attendance", description = "Mark attendance for a student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Attendance> markAttendance(@Valid @RequestBody AttendanceMarkingRequest request) {
        log.info("Marking attendance for student: {}", request.getStudentId());
        Attendance newAttendanceRecord = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAttendanceRecord);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Mark bulk attendance", description = "Mark attendance for multiple students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Attendance>> markBulkAttendance(@Valid @RequestBody List<AttendanceMarkingRequest> requests) {
        log.info("Marking bulk attendance for {} students", requests.size());
        List<Attendance> attendanceRecords = attendanceService.markBulkAttendance(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceRecords);
    }

    @GetMapping
    @Operation(summary = "Get all attendance records", description = "Get paginated attendance records")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Attendance>> getAllAttendance(
            @PageableDefault(size = 20, sort = "attendanceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching all attendance records");
        Page<Attendance> attendanceRecords = attendanceService.getAllAttendance(pageable);
        return ResponseEntity.ok(attendanceRecords);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get attendance by student", description = "Get attendance records for a specific student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Page<Attendance>> getAttendanceByStudent(
            @PathVariable UUID studentId,
            @PageableDefault(size = 20, sort = "attendanceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching attendance for student: {}", studentId);
        Page<Attendance> attendanceRecords = attendanceService.getAttendanceByStudent(studentId, pageable);
        return ResponseEntity.ok(attendanceRecords);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get attendance by class", description = "Get attendance records for a specific class")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Attendance>> getAttendanceByClass(
            @PathVariable UUID classId,
            @PageableDefault(size = 20, sort = "attendanceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching attendance for class: {}", classId);
        Page<Attendance> attendanceRecords = attendanceService.getAttendanceByClass(classId, pageable);
        return ResponseEntity.ok(attendanceRecords);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendance by date", description = "Get attendance records for a specific date")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Attendance>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "studentId", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching attendance for date: {}", date);
        Page<Attendance> attendanceRecords = attendanceService.getAttendanceByDate(date, pageable);
        return ResponseEntity.ok(attendanceRecords);
    }

    @GetMapping("/student/{studentId}/date-range")
    @Operation(summary = "Get attendance by date range", description = "Get attendance records for a student within date range")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Attendance>> getAttendanceByDateRange(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching attendance for student: {} from {} to {}", studentId, startDate, endDate);
        List<Attendance> attendanceRecords = attendanceService.getAttendanceByDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(attendanceRecords);
    }

    @GetMapping("/student/{studentId}/percentage")
    @Operation(summary = "Get attendance percentage", description = "Get attendance percentage for a student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getAttendancePercentage(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Calculating attendance percentage for student: {}", studentId);
        Map<String, Object> attendanceStats = attendanceService.getAttendancePercentage(studentId, startDate, endDate);
        return ResponseEntity.ok(attendanceStats);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update attendance", description = "Update existing attendance record")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable UUID id,
            @Valid @RequestBody AttendanceMarkingRequest request) {
        log.info("Updating attendance record: {}", id);
        Attendance updatedAttendance = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(updatedAttendance);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance", description = "Delete attendance record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable UUID id) {
        log.info("Deleting attendance record: {}", id);
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}