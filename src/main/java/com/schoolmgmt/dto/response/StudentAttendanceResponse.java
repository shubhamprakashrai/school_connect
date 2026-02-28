package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.StudentAttendance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for student attendance response.
 */
@Data
@Builder
@Schema(description = "Student attendance response")
public class StudentAttendanceResponse {

    @Schema(description = "Attendance record ID")
    private UUID id;

    @Schema(description = "Student ID")
    private UUID studentId;

    @Schema(description = "Student name")
    private String studentName;

    @Schema(description = "Roll number")
    private String rollNumber;

    @Schema(description = "Attendance date")
    private LocalDate attendanceDate;

    @Schema(description = "Section ID")
    private UUID sectionId;

    @Schema(description = "Section name")
    private String sectionName;

    @Schema(description = "Class name")
    private String className;

    @Schema(description = "Academic year ID")
    private UUID academicYearId;

    @Schema(description = "Academic year name")
    private String academicYearName;

    @Schema(description = "Attendance status")
    private StudentAttendance.AttendanceStatus status;

    @Schema(description = "Remarks/Reason")
    private String remarks;

    @Schema(description = "Marked by user ID")
    private String markedBy;

    @Schema(description = "Marked by user name")
    private String markedByName;

    @Schema(description = "Marked at")
    private LocalDateTime markedAt;

    @Schema(description = "Is half day")
    private Boolean isHalfDay;

    @Schema(description = "Half day type")
    private StudentAttendance.HalfDayType halfDayType;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}
