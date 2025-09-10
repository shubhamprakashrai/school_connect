package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "School class response")
public class SchoolClassResponse {
    
    @Schema(description = "Class ID")
    private UUID id;
    
    @Schema(description = "Class code", example = "10")
    private String code;
    
    @Schema(description = "Class name", example = "Class 10")
    private String name;
    
    @Schema(description = "Class description")
    private String description;
    
    @Schema(description = "List of sections in this class")
    private List<SectionResponse> sections;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}