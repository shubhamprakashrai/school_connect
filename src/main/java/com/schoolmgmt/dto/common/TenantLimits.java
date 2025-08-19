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
@Schema(description = "Tenant limits and usage")
public class TenantLimits {
    private Integer maxStudents;
    private Integer currentStudents;
    private Integer maxTeachers;
    private Integer currentTeachers;
    private Integer maxStorageGb;
    private Integer currentStorageMb;
    
    @Schema(description = "Percentage of student limit used")
    public int getStudentUsagePercentage() {
        return maxStudents > 0 ? (currentStudents * 100) / maxStudents : 0;
    }
    
    @Schema(description = "Percentage of teacher limit used")
    public int getTeacherUsagePercentage() {
        return maxTeachers > 0 ? (currentTeachers * 100) / maxTeachers : 0;
    }
    
    @Schema(description = "Percentage of storage limit used")
    public int getStorageUsagePercentage() {
        int maxMb = maxStorageGb * 1024;
        return maxMb > 0 ? (currentStorageMb * 100) / maxMb : 0;
    }
}
