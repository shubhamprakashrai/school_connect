package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for creating/updating an academic year.
 */
@Data
@Schema(description = "Academic year creation/update request")
public class AcademicYearRequest {

    @NotBlank(message = "Academic year name is required")
    @Schema(description = "Academic year name (e.g., 2024-2025)", example = "2024-2025")
    private String name;

    @NotNull(message = "Start date is required")
    @Schema(description = "Academic year start date", example = "2024-04-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Academic year end date", example = "2025-03-31")
    private LocalDate endDate;

    @Schema(description = "Is this the currently active academic year", example = "false")
    private Boolean isActive = false;
    

}
