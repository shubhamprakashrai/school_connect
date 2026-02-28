package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.StudentAttendance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for bulk marking attendance for multiple students in a section.
 */
@Data
@Schema(description = "Bulk attendance marking request for a section")
public class BulkAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    @Schema(description = "Attendance date", example = "2025-01-15")
    private LocalDate attendanceDate;

    @NotNull(message = "Section ID is required")
    @Schema(description = "Section ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID sectionId;

    @NotNull(message = "Academic year ID is required")
    @Schema(description = "Academic year ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID academicYearId;

    @NotNull(message = "Attendance records are required")
    @Schema(description = "List of student attendance records")
    private List<@Valid StudentAttendanceRecord> attendanceRecords;

    /**
     * Individual student attendance record within bulk request.
     */
    @Data
    @Schema(description = "Individual student attendance record")
    public static class StudentAttendanceRecord {

        @NotNull(message = "Student ID is required")
        @Schema(description = "Student ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID studentId;

        @NotNull(message = "Attendance status is required")
        @Schema(description = "Attendance status", example = "PRESENT")
        private StudentAttendance.AttendanceStatus status;

        @Schema(description = "Remarks/Reason", example = "Late by 10 minutes")
        private String remarks;

        @Schema(description = "Is half day attendance", example = "false")
        private Boolean isHalfDay;

        @Schema(description = "Half day type (if applicable)", example = "MORNING")
        private StudentAttendance.HalfDayType halfDayType;
    }
}
