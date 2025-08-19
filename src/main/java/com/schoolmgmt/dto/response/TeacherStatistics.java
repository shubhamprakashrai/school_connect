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
@Schema(description = "Teacher statistics")
public class TeacherStatistics {
    private long totalTeachers;
    private long activeTeachers;
    private Map<String, Long> teachersByDepartment;
    private Map<String, Long> teachersByEmployeeType;
    private Map<String, Double> averageRatingByDepartment;
    private long classTeachers;
    private double averageExperience;
}
