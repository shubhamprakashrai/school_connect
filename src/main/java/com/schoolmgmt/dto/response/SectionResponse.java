package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Section response")
public class SectionResponse {
    
    @Schema(description = "Section ID")
    private UUID id;
    
    @Schema(description = "Section name", example = "A")
    private String name;
    
    @Schema(description = "Section capacity", example = "40")
    private Integer capacity;
    
    @Schema(description = "School class ID")
    private UUID schoolClassId;
    
    @Schema(description = "School class code")
    private String schoolClassCode;
    
    @Schema(description = "School class name")
    private String schoolClassName;
    
    @Schema(description = "Class teacher ID")
    private UUID classTeacherId;
    
    @Schema(description = "Class teacher name")
    private String classTeacherName;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}