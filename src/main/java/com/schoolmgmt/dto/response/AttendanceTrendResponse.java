package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceTrendResponse {

    private List<DailyAttendance> trend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendance {
        private LocalDate date;
        private long presentCount;
        private long absentCount;
        private double percentage;
    }
}
