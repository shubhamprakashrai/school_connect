package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Promote student request")
public class PromoteStudentRequest {
    
    @NotBlank(message = "New class is required")
    @Schema(description = "New class ID")
    private String newClassId;
    
    @NotBlank(message = "New section is required")
    @Schema(description = "New section ID")
    private String newSectionId;
    
    @Schema(description = "New roll number")
    private String newRollNumber;
    
    @NotBlank(message = "Promotion status is required")
    @Pattern(regexp = "^(PROMOTED|DETAINED)$")
    @Schema(description = "Promotion status", example = "PROMOTED")
    private String promotionStatus;
}
