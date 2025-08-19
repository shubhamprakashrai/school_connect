package com.schoolmgmt.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Medical information")
public class MedicalInfo {
    
    @Schema(description = "Medical conditions")
    private String medicalConditions;
    
    @Schema(description = "Allergies")
    private String allergies;
    
    @Schema(description = "Emergency medication")
    private String emergencyMedication;
    
    @Schema(description = "Doctor name")
    private String doctorName;
    
    @Schema(description = "Doctor phone")
    private String doctorPhone;
}
