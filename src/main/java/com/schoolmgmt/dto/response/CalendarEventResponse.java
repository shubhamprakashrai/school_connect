package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.CalendarEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for calendar event response.
 */
@Data
@Builder
@Schema(description = "Calendar event response")
public class CalendarEventResponse {

    @Schema(description = "Calendar event ID")
    private UUID id;

    @Schema(description = "Event date")
    private LocalDate eventDate;

    @Schema(description = "End date for multi-day events")
    private LocalDate endDate;

    @Schema(description = "Event type")
    private CalendarEvent.EventType eventType;

    @Schema(description = "Event title")
    private String title;

    @Schema(description = "Event description")
    private String description;

    @Schema(description = "Is working day")
    private Boolean isWorkingDay;

    @Schema(description = "Academic year ID")
    private UUID academicYearId;

    @Schema(description = "Academic year name")
    private String academicYearName;

    @Schema(description = "Applicable sections")
    private String applicableSections;

    @Schema(description = "Is half day")
    private Boolean halfDay;

    @Schema(description = "Holiday type")
    private String holidayType;

    @Schema(description = "Created at")
    private LocalDate createdAt;

    @Schema(description = "Updated at")
    private LocalDate updatedAt;
}
