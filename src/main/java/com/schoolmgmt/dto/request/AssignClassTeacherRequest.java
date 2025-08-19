package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign class teacher request")
public class AssignClassTeacherRequest {
    
    @NotBlank(message = "Class ID is required")
    @Schema(description = "Class ID to assign", example = "class_10_a")
    private String classId;
}
