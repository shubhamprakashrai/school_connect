package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assignment submission response")
public class AssignmentSubmissionResponse {
    private String id;
    private String assignmentId;
    private String studentId;
    private LocalDateTime submissionDate;
    private String content;
    private String attachmentUrl;
    private Double marksObtained;
    private String feedback;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
