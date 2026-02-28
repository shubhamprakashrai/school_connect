package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO for updating teacher assignment.
 */
@Data
@Schema(description = "Teacher assignment update request")
public class TeacherAssignmentUpdateRequest {

    @Schema(description = "Is assignment active")
    private Boolean isActive;
}
