package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.Attendance;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AttendanceMarkingRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Teacher Class (Assignment) ID is required")
    private UUID teacherClassId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private Attendance.AttendanceStatus status;

    private String remarks;
}