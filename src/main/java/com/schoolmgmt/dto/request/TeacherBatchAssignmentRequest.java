package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for batch assigning multiple subjects to a teacher for a section.
 */
@Data
@Schema(description = "Batch teacher assignment request for multiple subjects")
public class TeacherBatchAssignmentRequest {

    @NotNull(message = "Teacher ID is required")
    @Schema(description = "Teacher ID to assign")
    private UUID teacherId;

    @NotNull(message = "Section ID is required")
    @Schema(description = "Section ID to assign teacher to")
    private UUID sectionId;

    @NotEmpty(message = "At least one subject is required")
    @Schema(description = "List of subject IDs to assign")
    private List<@NotNull UUID> subjectIds;

    @NotNull(message = "Academic Year ID is required")
    @Schema(description = "Academic Year ID")
    private UUID academicYearId;
}
