package com.schoolmgmt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a bulk student import operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Bulk import result")
public class BulkImportResult {

    @Schema(description = "Total number of rows processed", example = "50")
    private int totalRows;

    @Schema(description = "Number of successfully imported rows", example = "45")
    private int successCount;

    @Schema(description = "Number of rows with errors", example = "5")
    private int errorCount;

    @Schema(description = "List of import errors")
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();

    /**
     * Represents an error encountered during import for a specific row.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Import error details")
    public static class ImportError {

        @Schema(description = "Row number where the error occurred", example = "3")
        private int rowNumber;

        @Schema(description = "Field name that caused the error", example = "email")
        private String fieldName;

        @Schema(description = "Error message", example = "Invalid email format")
        private String errorMessage;

        @Schema(description = "Raw value that caused the error", example = "invalid-email")
        private String rawValue;
    }
}
