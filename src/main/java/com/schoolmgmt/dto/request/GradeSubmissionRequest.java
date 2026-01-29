package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Grade submission request")
public class GradeSubmissionRequest {

    @NotNull(message = "Marks obtained is required")
    @Schema(description = "Marks obtained", example = "85.0")
    private Double marksObtained;

    @Schema(description = "Feedback for the student")
    private String feedback;
}
