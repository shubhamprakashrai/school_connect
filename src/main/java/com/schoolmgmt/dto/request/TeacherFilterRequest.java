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
@Schema(description = "Teacher filter request")
public class TeacherFilterRequest {
    
    @Schema(description = "Filter by department")
    private String department;
    
    @Schema(description = "Filter by designation")
    private String designation;
    
    @Schema(description = "Filter by status")
    private String status;
    
    @Schema(description = "Filter by employee type")
    private String employeeType;
    
    @Schema(description = "Filter by subject")
    private String subject;
    
    @Schema(description = "Search by name or employee ID")
    private String search;
    
    @Schema(description = "Filter class teachers only")
    private Boolean classTeachersOnly;
}
