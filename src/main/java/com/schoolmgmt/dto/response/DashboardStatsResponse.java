package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalStudents;
    private long totalTeachers;
    private long totalParents;
    private long activeStudents;
    private double todayAttendancePercentage;
    private BigDecimal pendingFeeAmount;
    private BigDecimal collectedFeeAmount;
    private long pendingLeaveRequests;
    private long upcomingExams;
    private long recentAnnouncements;
}
