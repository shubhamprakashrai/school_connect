package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for teacher assignment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Teacher assignment response")
public class TeacherAssignmentResponse {

    @Schema(description = "Assignment ID")
    private UUID id;

    @Schema(description = "Teacher ID")
    private UUID teacherId;

    @Schema(description = "Teacher's full name")
    private String teacherName;

    @Schema(description = "Section ID")
    private UUID sectionId;

    @Schema(description = "Section name")
    private String sectionName;

    @Schema(description = "Subject ID")
    private UUID subjectId;

    @Schema(description = "Subject name")
    private String subjectName;

    @Schema(description = "Academic year ID")
    private UUID academicYearId;

    @Schema(description = "Academic year name")
    private String academicYearName;

    @Schema(description = "Is assignment active")
    private Boolean isActive;

    @Schema(description = "Assignment creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Assignment last update date")
    private LocalDateTime updatedAt;
}
