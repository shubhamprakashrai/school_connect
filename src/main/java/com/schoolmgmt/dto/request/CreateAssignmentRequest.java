package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create assignment request")
public class CreateAssignmentRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Assignment title", example = "Math Homework Chapter 5")
    private String title;

    @Schema(description = "Assignment description", example = "Complete exercises 1-20 from Chapter 5")
    private String description;

    @Schema(description = "Subject ID")
    private String subjectId;

    @NotBlank(message = "Class ID is required")
    @Schema(description = "Class ID", example = "class_10")
    private String classId;

    @Schema(description = "Section ID")
    private String sectionId;

    @NotBlank(message = "Teacher ID is required")
    @Schema(description = "Teacher ID")
    private String teacherId;

    @NotNull(message = "Due date is required")
    @Schema(description = "Due date", example = "2026-02-15")
    private LocalDate dueDate;

    @Schema(description = "Assigned date", example = "2026-01-29")
    private LocalDate assignedDate;

    @Schema(description = "Maximum marks", example = "100")
    private Integer maxMarks;

    @Schema(description = "Attachment URL")
    private String attachmentUrl;

    @Schema(description = "Assignment status", example = "PUBLISHED")
    private String status;

    @Schema(description = "Assignment type", example = "HOMEWORK")
    private String type;
}
