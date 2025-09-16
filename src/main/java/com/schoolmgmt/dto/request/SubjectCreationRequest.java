package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject creation request")
public class SubjectCreationRequest {

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    @Schema(description = "Subject name", example = "Mathematics")
    private String name;

    @NotBlank(message = "Subject code is required")
    @Size(min = 2, max = 20, message = "Subject code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Subject code must contain only uppercase letters, numbers, and underscores")
    @Schema(description = "Unique subject code", example = "MATH10")
    private String code;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Subject description", example = "Class 10 Mathematics covering algebra, geometry and statistics")
    private String description;

    @Schema(description = "Subject type", example = "CORE", allowableValues = {"CORE", "ELECTIVE", "EXTRA_CURRICULAR"})
    private String type;

    @Schema(description = "Credit hours for this subject", example = "4")
    private Integer creditHours;

    @Schema(description = "Maximum marks for this subject", example = "100")
    private Integer maxMarks;

    @Schema(description = "Minimum passing marks", example = "35")
    private Integer passingMarks;

    @Schema(description = "Academic year", example = "2025-26")
    private String academicYear;

    @Schema(description = "List of class IDs this subject is assigned to")
    private List<UUID> classIds;

    @Schema(description = "List of teacher IDs assigned to this subject")
    private List<UUID> teacherIds;

    @Schema(description = "Department this subject belongs to", example = "Science")
    private String department;

    @Schema(description = "Whether this subject is active", example = "true")
    @Builder.Default
    private Boolean isActive = true;

    @Schema(description = "Subject prerequisites")
    private List<String> prerequisites;

    @Schema(description = "Learning objectives")
    private List<String> learningObjectives;
}