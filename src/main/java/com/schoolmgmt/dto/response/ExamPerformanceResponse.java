package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPerformanceResponse {

    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private List<SubjectWiseAverage> subjectWiseAverage;
    private Map<String, Long> gradeDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectWiseAverage {
        private String subject;
        private double average;
    }
}
