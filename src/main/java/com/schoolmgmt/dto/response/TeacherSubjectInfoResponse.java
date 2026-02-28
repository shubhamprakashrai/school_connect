package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for teacher subject information - used in teacher profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Teacher subject information for profile")
public class TeacherSubjectInfoResponse {

    @Schema(description = "Teacher ID")
    private UUID teacherId;

    @Schema(description = "Teacher name")
    private String teacherName;

    @Schema(description = "Total number of unique subjects taught")
    private int totalSubjectsCount;

    @Schema(description = "Total number of section assignments")
    private int totalAssignmentsCount;

    @Schema(description = "List of subjects taught by teacher")
    private List<SubjectInfo> subjects;

    @Schema(description = "List of sections where teacher is assigned")
    private List<SectionInfo> sections;

    /**
     * Subject details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectInfo {
        @Schema(description = "Subject ID")
        private UUID subjectId;

        @Schema(description = "Subject name")
        private String subjectName;

        @Schema(description = "Subject code")
        private String subjectCode;
    }

    /**
     * Section details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionInfo {
        @Schema(description = "Section ID")
        private UUID sectionId;

        @Schema(description = "Section name")
        private String sectionName;

        @Schema(description = "Class name")
        private String className;

        @Schema(description = "List of subjects taught in this section")
        private List<String> subjectsInSection;
    }
}
