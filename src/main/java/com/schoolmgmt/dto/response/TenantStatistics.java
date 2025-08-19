package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant statistics")
public class TenantStatistics {
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;
    private long activeUsers;
    private long totalClasses;
    private double attendancePercentage;
    private long storageUsedMb;
    private Map<String, Long> usersByRole;
    private Map<String, Long> studentsByClass;
}
