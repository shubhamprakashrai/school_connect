package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student filter request")
public class StudentFilterRequest {
    
    @Schema(description = "Filter by class ID")
    private String classId;
    
    @Schema(description = "Filter by section ID")
    private String sectionId;
    
    @Schema(description = "Filter by status")
    private String status;
    
    @Schema(description = "Search by name or admission number")
    private String search;
    
    @Schema(description = "Filter by gender")
    private String gender;
    
    @Schema(description = "Filter by fee category")
    private String feeCategory;
    
    @Schema(description = "Filter by transport route")
    private String transportRouteId;
}
