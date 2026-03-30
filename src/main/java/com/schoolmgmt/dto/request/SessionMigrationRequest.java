package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to migrate to a new academic session")
public class SessionMigrationRequest {

    @NotBlank(message = "New year name is required")
    @Schema(description = "Name of the new academic year", example = "2025-2026")
    private String newYearName;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date of the new academic year", example = "2025-04-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "End date of the new academic year", example = "2026-03-31")
    private LocalDate endDate;
}
