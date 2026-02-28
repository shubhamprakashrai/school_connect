package com.schoolmgmt.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.schoolmgmt.model.Attendance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Custom UUID deserializer that handles invalid UUID formats gracefully
 */
class UUIDDeserializer extends JsonDeserializer<UUID> {
    @Override
    public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Check for placeholder values
        if (value.contains("uuid-of-teacher-here") || 
            value.contains("ACTUAL-TEACHER-UUID-HERE") ||
            value.contains("invalid-teacher-uuid") ||
            !isValidUUIDFormat(value)) {
            
            // Return a placeholder UUID that will be caught in validation
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            // Return placeholder UUID for invalid format
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }
    
    private boolean isValidUUIDFormat(String value) {
        return value != null && 
               value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}

/**
 * DTO for bulk marking attendance for multiple teachers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk teacher attendance marking request")
public class BulkTeacherAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    @Schema(description = "Attendance date", example = "2025-01-15")
    private LocalDate attendanceDate;

    @Schema(description = "Optional reference for tracking this bulk operation", example = "TEACHER-ATTENDANCE-2024-001")
    private String batchReference;

    @Schema(description = "Continue processing even if some records fail", example = "true")
    private Boolean continueOnError = true;

    @NotNull(message = "Teacher attendance records are required")
    @Schema(description = "List of teacher attendance records")
    private List<@Valid TeacherAttendanceRecord> attendanceRecords;

    /**
     * Individual teacher attendance record within bulk request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual teacher attendance record")
    public static class TeacherAttendanceRecord {

        @JsonDeserialize(using = UUIDDeserializer.class)
        @Schema(description = "Teacher ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID teacherId;

        @NotNull(message = "Attendance status is required")
        @Schema(description = "Attendance status", example = "PRESENT")
        private Attendance.AttendanceStatus status;

        @Schema(description = "Remarks/Reason", example = "Late by 10 minutes")
        private String remarks;

        @Schema(description = "Check-in time", example = "08:30")
        private String checkInTime;

        @Schema(description = "Check-out time", example = "16:30")
        private String checkOutTime;

        @Schema(description = "Is half day attendance", example = "false")
        private Boolean isHalfDay;

        @Schema(description = "Half day type (if applicable)", example = "MORNING")
        private String halfDayType;
    }
}
