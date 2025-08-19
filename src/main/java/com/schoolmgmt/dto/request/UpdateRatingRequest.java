package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update teacher rating request")
public class UpdateRatingRequest {
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Schema(description = "Rating (0-5)", example = "4.5")
    private Double rating;
    
    @Schema(description = "Evaluation comments")
    private String comments;
}
