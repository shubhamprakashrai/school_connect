package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.CalendarEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating/updating a calendar event.
 */
@Data
@Schema(description = "Calendar event creation/update request")
public class CalendarEventRequest {

    @NotNull(message = "Event date is required")
    @Schema(description = "Event date", example = "2025-01-26")
    private LocalDate eventDate;

    @Schema(description = "End date for multi-day events (optional)", example = "2025-01-28")
    private LocalDate endDate;

    @NotNull(message = "Event type is required")
    @Schema(description = "Event type", example = "HOLIDAY", allowableValues = {"WORKING_DAY", "HOLIDAY", "HALF_DAY", "EXAM", "EVENT", "TEACHER_MEETING", "SPORTS_DAY", "PARENT_TEACHER_MEETING"})
    private CalendarEvent.EventType eventType;

    @NotBlank(message = "Title is required")
    @Schema(description = "Event title", example = "Republic Day")
    private String title;

    @Schema(description = "Event description", example = "National holiday - School closed")
    private String description;

    @Schema(description = "Is this a working day", example = "false")
    private Boolean isWorkingDay;

    @Schema(description = "Academic year ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID academicYearId;

    @Schema(description = "Applicable section IDs (comma-separated, null means all sections)", example = "section1-uuid,section2-uuid")
    private String applicableSections;

    @Schema(description = "Is this a half day", example = "false")
    private Boolean halfDay;

    @Schema(description = "Holiday type", example = "NATIONAL", allowableValues = {"NATIONAL", "REGIONAL", "RELIGIOUS", "SCHOOL_EVENT", "TEACHER_TRAINING"})
    private String holidayType;
}
