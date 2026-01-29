package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assignment response")
public class AssignmentResponse {
    private String id;
    private String title;
    private String description;
    private String subjectId;
    private String classId;
    private String sectionId;
    private String teacherId;
    private LocalDate dueDate;
    private LocalDate assignedDate;
    private Integer maxMarks;
    private String attachmentUrl;
    private String status;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
