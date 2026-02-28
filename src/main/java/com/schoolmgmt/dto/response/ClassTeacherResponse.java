package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for class teacher assignment information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Class teacher assignment response")
public class ClassTeacherResponse {

    @Schema(description = "Section ID")
    private UUID sectionId;

    @Schema(description = "Section name")
    private String sectionName;

    @Schema(description = "Class name (e.g., Grade 10)")
    private String className;

    @Schema(description = "Teacher ID")
    private UUID teacherId;

    @Schema(description = "Teacher full name")
    private String teacherName;

    @Schema(description = "Teacher employee ID")
    private String employeeId;

    @Schema(description = "Academic year ID")
    private UUID academicYearId;

    @Schema(description = "Academic year name")
    private String academicYearName;

    @Schema(description = "Assignment remarks")
    private String remarks;

    @Schema(description = "Assignment creation timestamp")
    private LocalDateTime assignedAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
