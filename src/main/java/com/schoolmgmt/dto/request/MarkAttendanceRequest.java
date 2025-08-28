package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mark attendance request")
public class MarkAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    @Schema(description = "Date for attendance", example = "2024-01-15")
    private LocalDate attendanceDate;

    @NotBlank(message = "Class ID is required")
    @Schema(description = "Class identifier", example = "class-10")
    private String classId;

    @Schema(description = "Section identifier", example = "section-a")
    private String sectionId;

    @Schema(description = "Subject name for subject-wise attendance", example = "Mathematics")
    private String subject;

    @Pattern(regexp = "^(MORNING|AFTERNOON|FULL_DAY)$", message = "Invalid session type")
    @Schema(description = "Attendance session", example = "FULL_DAY")
    @Builder.Default
    private String session = "FULL_DAY";

    @NotEmpty(message = "Student attendance list cannot be empty")
    @Valid
    @Schema(description = "List of student attendance records")
    private List<StudentAttendanceRecord> studentAttendance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual student attendance record")
    public static class StudentAttendanceRecord {

        @NotNull(message = "Student ID is required")
        @Schema(description = "Student identifier", example = "uuid-here")
        private String studentId;

        @NotBlank(message = "Attendance status is required")
        @Pattern(regexp = "^(PRESENT|ABSENT|LATE|HALF_DAY|EXCUSED)$", message = "Invalid attendance status")
        @Schema(description = "Attendance status", example = "PRESENT")
        private String status;

        @Schema(description = "Optional remarks", example = "Late due to medical appointment")
        private String remarks;
    }
}
