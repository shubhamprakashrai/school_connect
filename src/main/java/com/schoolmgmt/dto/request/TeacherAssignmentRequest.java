package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TeacherAssignmentRequest {

    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;

    @NotNull(message = "Section ID is required")
    private UUID sectionId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "Academic Year ID is required")
    private UUID academicYearId;
}