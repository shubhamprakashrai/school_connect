package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance statistics response")
public class AttendanceStatistics {

    private LocalDate date;
    private String classId;
    private String sectionId;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Integer lateCount;
    private Integer halfDayCount;
    private Integer excusedCount;
    private Double attendancePercentage;
    
    // Daily statistics for a date range
    private Map<LocalDate, DailyAttendance> dailyAttendance;
    
    // Student-wise statistics
    private Map<String, StudentAttendanceStats> studentStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAttendance {
        private Integer totalStudents;
        private Integer presentCount;
        private Integer absentCount;
        private Double attendancePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceStats {
        private String studentId;
        private String studentName;
        private String rollNumber;
        private Integer totalDays;
        private Integer presentDays;
        private Integer absentDays;
        private Integer lateDays;
        private Double attendancePercentage;
    }
}
