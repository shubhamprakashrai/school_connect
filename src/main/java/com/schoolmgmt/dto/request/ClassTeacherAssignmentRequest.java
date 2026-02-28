package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for assigning a class teacher to a section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for assigning a class teacher to a section")
public class ClassTeacherAssignmentRequest {

    @NotNull(message = "Section ID is required")
    @Schema(description = "Section ID to assign the class teacher to", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID sectionId;

    @NotNull(message = "Teacher ID is required")
    @Schema(description = "Teacher ID to assign as class teacher", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID teacherId;

    @Schema(description = "Academic year ID for this assignment", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID academicYearId;

    @Schema(description = "Optional remarks for this assignment", example = "Primary class teacher for academic year 2024-2025")
    private String remarks;
}
