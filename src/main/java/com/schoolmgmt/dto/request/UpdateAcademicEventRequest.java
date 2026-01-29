package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update academic event request")
public class UpdateAcademicEventRequest {

    @Size(max = 200)
    @Schema(description = "Event title")
    private String title;

    @Size(max = 1000)
    @Schema(description = "Event description")
    private String description;

    @Schema(description = "Event type")
    private String eventType;

    @Schema(description = "Start date")
    private LocalDate startDate;

    @Schema(description = "End date")
    private LocalDate endDate;

    @Schema(description = "Is this an all-day event")
    private Boolean isAllDay;

    @Schema(description = "Is this a recurring event")
    private Boolean isRecurring;

    @Schema(description = "Color hex code")
    private String color;

    @Schema(description = "Target audience")
    private String targetAudience;

    @Schema(description = "Class ID (null means all classes)")
    private String classId;

    @Schema(description = "Academic year")
    private String academicYear;
}
