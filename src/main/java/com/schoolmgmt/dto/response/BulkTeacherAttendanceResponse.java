package com.schoolmgmt.dto.response;

import com.schoolmgmt.dto.request.BulkTeacherAttendanceRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for bulk teacher attendance operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk teacher attendance operation response")
public class BulkTeacherAttendanceResponse {

    @Schema(description = "Batch reference for tracking", example = "TEACHER-ATTENDANCE-2024-001")
    private String batchReference;

    @Schema(description = "Total number of teacher attendance records requested", example = "50")
    private int totalRequested;

    @Schema(description = "Number of teacher attendance records successfully marked", example = "47")
    private int successful;

    @Schema(description = "Number of teacher attendance records that failed", example = "3")
    private int failed;

    @Schema(description = "List of successfully created teacher attendance records")
    private List<AttendanceResponse> createdAttendanceRecords;

    @Schema(description = "List of errors for failed teacher attendance records")
    private List<BulkError> errors;

    @Schema(description = "Processing summary by department/role")
    private Map<String, DepartmentSummary> departmentSummaries;

    @Schema(description = "Timestamp when the bulk operation completed")
    private LocalDateTime processedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error details for failed teacher attendance marking")
    public static class BulkError {
        @Schema(description = "Index in the original request (0-based)", example = "5")
        private int rowIndex;

        @Schema(description = "Teacher ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID teacherId;

        @Schema(description = "Teacher identifier for reference", example = "John Doe")
        private String teacherIdentifier;

        @Schema(description = "Error message", example = "Teacher not found with ID: 550e8400-e29b-41d4-a716-446655440000")
        private String errorMessage;

        @Schema(description = "Error type", example = "TEACHER_NOT_FOUND")
        private String errorType;

        @Schema(description = "The teacher attendance request that failed")
        private BulkTeacherAttendanceRequest.TeacherAttendanceRecord failedRequest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Processing summary for each department")
    public static class DepartmentSummary {
        @Schema(description = "Department name", example = "Mathematics")
        private String departmentName;

        @Schema(description = "Number of teachers requested in this department", example = "10")
        private int totalRequested;

        @Schema(description = "Number of teachers successfully marked in this department", example = "9")
        private int successful;

        @Schema(description = "Number of teachers that failed in this department", example = "1")
        private int failed;
    }
}
