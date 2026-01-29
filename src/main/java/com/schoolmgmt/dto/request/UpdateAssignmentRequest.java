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
@Schema(description = "Update assignment request")
public class UpdateAssignmentRequest {

    @Schema(description = "Assignment title")
    private String title;

    @Schema(description = "Assignment description")
    private String description;

    @Schema(description = "Subject ID")
    private String subjectId;

    @Schema(description = "Class ID")
    private String classId;

    @Schema(description = "Section ID")
    private String sectionId;

    @Schema(description = "Due date")
    private LocalDate dueDate;

    @Schema(description = "Maximum marks")
    private Integer maxMarks;

    @Schema(description = "Attachment URL")
    private String attachmentUrl;

    @Schema(description = "Assignment status", example = "PUBLISHED")
    private String status;

    @Schema(description = "Assignment type", example = "HOMEWORK")
    private String type;
}
