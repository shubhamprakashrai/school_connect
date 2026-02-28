package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for attendance summary response.
 */
@Data
@Builder
@Schema(description = "Attendance summary response")
public class AttendanceSummaryResponse {

    @Schema(description = "Student ID (null for section summary)")
    private UUID studentId;

    @Schema(description = "Student name (null for section summary)")
    private String studentName;

    @Schema(description = "Section ID")
    private UUID sectionId;

    @Schema(description = "Section name")
    private String sectionName;

    @Schema(description = "Start date of summary period")
    private LocalDate startDate;

    @Schema(description = "End date of summary period")
    private LocalDate endDate;

    @Schema(description = "Total working days in period")
    private Long totalWorkingDays;

    @Schema(description = "Total present days")
    private Long presentDays;

    @Schema(description = "Total absent days")
    private Long absentDays;

    @Schema(description = "Total leave days")
    private Long leaveDays;

    @Schema(description = "Total late days")
    private Long lateDays;

    @Schema(description = "Total half days")
    private Long halfDays;

    @Schema(description = "Attendance percentage")
    private Double attendancePercentage;

    @Schema(description = "Daily breakdown - date to status")
    private Map<LocalDate, String> dailyBreakdown;
}
