package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create school class request")
public class CreateSchoolClassRequest {
    
    @NotBlank(message = "Class code is required")
    @Size(max = 20)
    @Schema(description = "Unique class code", example = "10")
    private String code;
    
    @NotBlank(message = "Class name is required")
    @Size(max = 100)
    @Schema(description = "Full class name", example = "Class 10")
    private String name;
    
    @Size(max = 500)
    @Schema(description = "Class description", example = "Secondary level class")
    private String description;
    
    @Schema(description = "List of sections to create with this class")
    private List<CreateSectionRequest> sections;
}