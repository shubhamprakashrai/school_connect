package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Get attendance filter request")
public class AttendanceFilterRequest {

    @Schema(description = "Class identifier", example = "class-10")
    private String classId;

    @Schema(description = "Section identifier", example = "section-a")
    private String sectionId;

    @Schema(description = "Start date for attendance range", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "End date for attendance range", example = "2024-01-31")
    private LocalDate endDate;

    @Schema(description = "Student identifier", example = "uuid-here")
    private String studentId;

    @Schema(description = "Attendance status filter", example = "PRESENT")
    private String status;

    @Schema(description = "Subject name", example = "Mathematics")
    private String subject;

    @Schema(description = "Session type", example = "FULL_DAY")
    private String session;
}
