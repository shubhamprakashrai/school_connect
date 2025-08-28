package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assign teacher to class request")
public class AssignTeacherToClassRequest {

    @NotNull(message = "Teacher ID is required")
    @Schema(description = "Teacher identifier", example = "uuid-here")
    private UUID teacherId;

    @NotBlank(message = "Class ID is required")
    @Schema(description = "Class identifier", example = "class-10")
    private String classId;

    @Schema(description = "Section identifier", example = "section-a")
    private String sectionId;

    @Schema(description = "Subject name", example = "Mathematics")
    private String subject;

    @Schema(description = "Is this teacher the main class teacher?", example = "false")
    @Builder.Default
    private Boolean isClassTeacher = false;

    @Schema(description = "Academic year", example = "2024-2025")
    private String academicYear;

    @Schema(description = "Assignment start date", example = "2024-01-01")
    private LocalDate assignedDate;

    @Schema(description = "Optional remarks", example = "Assigned for Mathematics and Science")
    private String remarks;
}
