package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Create academic event request")
public class CreateAcademicEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Schema(description = "Event title", example = "Diwali Holiday")
    private String title;

    @Size(max = 1000)
    @Schema(description = "Event description", example = "School closed for Diwali celebrations")
    private String description;

    @NotBlank(message = "Event type is required")
    @Schema(description = "Event type", example = "HOLIDAY")
    private String eventType;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date", example = "2025-10-20")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "End date", example = "2025-10-25")
    private LocalDate endDate;

    @Schema(description = "Is this an all-day event", example = "true")
    @Builder.Default
    private Boolean isAllDay = true;

    @Schema(description = "Is this a recurring event", example = "false")
    @Builder.Default
    private Boolean isRecurring = false;

    @Schema(description = "Color hex code", example = "#FF0000")
    private String color;

    @Schema(description = "Target audience", example = "ALL")
    @Builder.Default
    private String targetAudience = "ALL";

    @Schema(description = "Class ID (null means all classes)")
    private String classId;

    @Schema(description = "Academic year", example = "2024-2025")
    private String academicYear;
}
