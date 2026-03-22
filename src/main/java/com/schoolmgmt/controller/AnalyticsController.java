package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.response.*;
import com.schoolmgmt.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard Analytics", description = "APIs for dashboard analytics and statistics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get dashboard stats",
               description = "Retrieve main dashboard statistics including student, teacher, fee, and attendance data")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        DashboardStatsResponse stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    @GetMapping("/attendance/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get attendance trend",
               description = "Retrieve daily attendance trend data for the specified number of days")
    public ResponseEntity<ApiResponse> getAttendanceTrend(
            @Parameter(description = "Number of days for the trend (default 30)")
            @RequestParam(defaultValue = "30") int days) {
        log.info("Fetching attendance trend for last {} days", days);
        if (days < 1 || days > 365) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Days parameter must be between 1 and 365"));
        }
        AttendanceTrendResponse trend = analyticsService.getAttendanceTrend(days);
        return ResponseEntity.ok(ApiResponse.success("Attendance trend retrieved successfully", trend));
    }

    @GetMapping("/fee/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get fee collection summary",
               description = "Retrieve fee collection summary including monthly breakdown and payment mode distribution")
    public ResponseEntity<ApiResponse> getFeeSummary() {
        log.info("Fetching fee analytics summary");
        FeeAnalyticsResponse summary = analyticsService.getFeeSummary();
        return ResponseEntity.ok(ApiResponse.success("Fee summary retrieved successfully", summary));
    }

    @GetMapping("/student/demographics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get student demographics",
               description = "Retrieve student demographics including class-wise count, gender distribution, and new admissions")
    public ResponseEntity<ApiResponse> getStudentDemographics() {
        log.info("Fetching student demographics");
        StudentDemographicsResponse demographics = analyticsService.getStudentDemographics();
        return ResponseEntity.ok(ApiResponse.success("Student demographics retrieved successfully", demographics));
    }

    @GetMapping("/exam/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get exam performance overview",
               description = "Retrieve exam performance overview including averages, subject-wise scores, and grade distribution")
    public ResponseEntity<ApiResponse> getExamPerformance() {
        log.info("Fetching exam performance overview");
        ExamPerformanceResponse performance = analyticsService.getExamPerformance();
        return ResponseEntity.ok(ApiResponse.success("Exam performance retrieved successfully", performance));
    }
}
