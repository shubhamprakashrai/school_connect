package com.schoolmgmt.dto.response;

import com.schoolmgmt.dto.request.CreateStudentRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk student creation response")
public class BulkStudentResponse {
    
    @Schema(description = "Batch reference for tracking", example = "BATCH-2024-001")
    private String batchReference;
    
    @Schema(description = "Total number of students requested", example = "50")
    private int totalRequested;
    
    @Schema(description = "Number of students successfully created", example = "47")
    private int successful;
    
    @Schema(description = "Number of students that failed to create", example = "3")
    private int failed;
    
    @Schema(description = "List of successfully created students")
    private List<StudentResponse> createdStudents;
    
    @Schema(description = "List of failed students with error details")
    private List<BulkError> errors;
    
    @Schema(description = "Processing summary by section")
    private List<SectionSummary> sectionSummaries;
    
    @Schema(description = "Timestamp when the bulk operation completed")
    private LocalDateTime processedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error details for failed student creation")
    public static class BulkError {
        @Schema(description = "Row index in the original request (0-based)", example = "5")
        private int rowIndex;
        
        @Schema(description = "Error message", example = "Email already registered: john@example.com")
        private String errorMessage;
        
        @Schema(description = "Error type", example = "DUPLICATE_EMAIL")
        private String errorType;
        
        @Schema(description = "The student request that failed")
        private CreateStudentRequest failedRequest;
        
        @Schema(description = "Student identifier for reference", example = "John Doe")
        private String studentIdentifier;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Processing summary for each section")
    public static class SectionSummary {
        @Schema(description = "Section ID")
        private String sectionId;
        
        @Schema(description = "Section name", example = "10-A")
        private String sectionName;
        
        @Schema(description = "Number of students requested in this section", example = "25")
        private int totalRequested;
        
        @Schema(description = "Number of students successfully created in this section", example = "23")
        private int successful;
        
        @Schema(description = "Number of students that failed in this section", example = "2")
        private int failed;
    }
}
