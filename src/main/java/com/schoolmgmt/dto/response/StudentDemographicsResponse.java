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
public class StudentDemographicsResponse {

    private List<ClassWiseCount> classWiseCount;
    private Map<String, Long> genderDistribution;
    private long newAdmissionsThisMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassWiseCount {
        private String className;
        private long count;
    }
}
