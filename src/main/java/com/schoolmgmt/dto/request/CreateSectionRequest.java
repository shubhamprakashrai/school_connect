package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create section request")
public class CreateSectionRequest {
    
    @NotBlank(message = "Section name is required")
    @Size(max = 50)
    @Schema(description = "Section name", example = "A")
    private String name;
    
    @Schema(description = "Section capacity", example = "40")
    private Integer capacity;
    
    @Schema(description = "School class ID (if creating section independently)")
    private UUID schoolClassId;
}