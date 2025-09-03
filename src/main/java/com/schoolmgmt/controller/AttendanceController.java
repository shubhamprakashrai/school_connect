package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.AttendanceMarkingRequest;
import com.schoolmgmt.model.Attendance;
import com.schoolmgmt.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Attendance> markAttendance(@Valid @RequestBody AttendanceMarkingRequest request) {
        Attendance newAttendanceRecord = attendanceService.markAttendance(request);
        return new ResponseEntity<>(newAttendanceRecord, HttpStatus.CREATED);
    }

    // Endpoints for fetching attendance records would go here.
}