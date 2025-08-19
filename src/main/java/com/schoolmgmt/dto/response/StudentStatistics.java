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
@Schema(description = "Student statistics")
public class StudentStatistics {
    private long totalStudents;
    private long activeStudents;
    private long maleStudents;
    private long femaleStudents;
    private Map<String, Long> studentsByClass;
    private Map<String, Long> studentsByStatus;
}
