package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to promote multiple students")
public class BulkPromoteStudentsRequest {

    @NotEmpty(message = "Student promotions list cannot be empty")
    @Valid
    @Schema(description = "List of student promotion details")
    private List<StudentPromotionEntry> studentPromotions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual student promotion entry")
    public static class StudentPromotionEntry {

        @NotNull(message = "Student ID is required")
        @Schema(description = "ID of the student to promote")
        private UUID studentId;

        @Schema(description = "New class ID for the student")
        private String newClassId;

        @Schema(description = "New section ID for the student")
        private String newSectionId;

        @Schema(description = "New roll number for the student")
        private String newRollNumber;

        @NotNull(message = "Promotion status is required")
        @Schema(description = "Promotion status: PROMOTED or DETAINED", example = "PROMOTED")
        private String promotionStatus;
    }
}
