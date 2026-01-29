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
@Schema(description = "Submit assignment request")
public class SubmitAssignmentRequest {

    @NotBlank(message = "Student ID is required")
    @Schema(description = "Student ID")
    private String studentId;

    @Schema(description = "Submission content")
    private String content;

    @Schema(description = "Attachment URL")
    private String attachmentUrl;
}
