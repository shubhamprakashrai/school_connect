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

}