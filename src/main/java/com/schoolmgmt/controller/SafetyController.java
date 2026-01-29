package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.model.CounselingReferral;
import com.schoolmgmt.model.EmergencyAlert;
import com.schoolmgmt.model.IncidentReport;
import com.schoolmgmt.model.User;
import com.schoolmgmt.service.SafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for safety-related operations.
 */
@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Safety", description = "Safety management APIs - incidents, counseling, emergency alerts")
@SecurityRequirement(name = "bearerAuth")
public class SafetyController {

    private final SafetyService safetyService;

    // ==================== INCIDENT REPORTS ====================

    @PostMapping("/incidents")
    @Operation(summary = "Create incident report", description = "Report a safety incident")
    public ResponseEntity<ApiResponse> createIncident(
            @AuthenticationPrincipal User user,
            @RequestBody IncidentReport report) {

        IncidentReport created = safetyService.createIncidentReport(report, user);
        return ResponseEntity.ok(ApiResponse.success("Incident reported successfully", created));
    }

    @GetMapping("/incidents")
    @Operation(summary = "Get all incidents", description = "Get all incident reports with pagination")
    public ResponseEntity<ApiResponse> getIncidents(Pageable pageable) {
        Page<IncidentReport> incidents = safetyService.getIncidentReports(pageable);
        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved successfully", incidents));
    }

    @GetMapping("/incidents/{id}")
    @Operation(summary = "Get incident by ID", description = "Get incident report details")
    public ResponseEntity<ApiResponse> getIncident(@PathVariable UUID id) {
        IncidentReport incident = safetyService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success("Incident retrieved successfully", incident));
    }

    @PatchMapping("/incidents/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update incident status", description = "Update incident report status")
    public ResponseEntity<ApiResponse> updateIncidentStatus(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @RequestParam IncidentReport.IncidentStatus status,
            @RequestParam(required = false) String notes) {

        IncidentReport updated = safetyService.updateIncidentStatus(id, status, notes, user);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }

    @GetMapping("/incidents/status/{status}")
    @Operation(summary = "Get incidents by status", description = "Get incidents filtered by status")
    public ResponseEntity<ApiResponse> getIncidentsByStatus(
            @PathVariable IncidentReport.IncidentStatus status) {

        List<IncidentReport> incidents = safetyService.getIncidentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved successfully", incidents));
    }

    // ==================== COUNSELING REFERRALS ====================

    @PostMapping("/counseling")
    @Operation(summary = "Create counseling referral", description = "Create a counseling referral for a student")
    public ResponseEntity<ApiResponse> createReferral(
            @AuthenticationPrincipal User user,
            @RequestBody CounselingReferral referral) {

        CounselingReferral created = safetyService.createCounselingReferral(referral, user);
        return ResponseEntity.ok(ApiResponse.success("Counseling referral created successfully", created));
    }

    @GetMapping("/counseling")
    @Operation(summary = "Get all referrals", description = "Get all counseling referrals with pagination")
    public ResponseEntity<ApiResponse> getReferrals(Pageable pageable) {
        Page<CounselingReferral> referrals = safetyService.getCounselingReferrals(pageable);
        return ResponseEntity.ok(ApiResponse.success("Referrals retrieved successfully", referrals));
    }

    @GetMapping("/counseling/{id}")
    @Operation(summary = "Get referral by ID", description = "Get counseling referral details")
    public ResponseEntity<ApiResponse> getReferral(@PathVariable UUID id) {
        CounselingReferral referral = safetyService.getCounselingById(id);
        return ResponseEntity.ok(ApiResponse.success("Referral retrieved successfully", referral));
    }

    @PatchMapping("/counseling/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update referral status", description = "Update counseling referral status")
    public ResponseEntity<ApiResponse> updateReferralStatus(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @RequestParam CounselingReferral.ReferralStatus status,
            @RequestParam(required = false) String notes) {

        CounselingReferral updated = safetyService.updateCounselingStatus(id, status, notes, user);
        return ResponseEntity.ok(ApiResponse.success("Referral status updated", updated));
    }

    @GetMapping("/counseling/pending")
    @Operation(summary = "Get pending referrals", description = "Get all pending counseling referrals")
    public ResponseEntity<ApiResponse> getPendingReferrals() {
        List<CounselingReferral> referrals = safetyService.getPendingReferrals();
        return ResponseEntity.ok(ApiResponse.success("Pending referrals retrieved", referrals));
    }

    @GetMapping("/counseling/student/{studentId}")
    @Operation(summary = "Get student referrals", description = "Get all referrals for a student")
    public ResponseEntity<ApiResponse> getStudentReferrals(
            @PathVariable String studentId) {

        List<CounselingReferral> referrals = safetyService.getStudentReferrals(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student referrals retrieved", referrals));
    }

    // ==================== EMERGENCY ALERTS ====================

    @PostMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Trigger emergency alert", description = "Trigger an emergency alert")
    public ResponseEntity<ApiResponse> triggerAlert(
            @AuthenticationPrincipal User user,
            @RequestBody EmergencyAlert alert) {

        EmergencyAlert created = safetyService.triggerEmergencyAlert(alert, user);
        return ResponseEntity.ok(ApiResponse.success("Emergency alert triggered", created));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get all alerts", description = "Get all emergency alerts with pagination")
    public ResponseEntity<ApiResponse> getAlerts(Pageable pageable) {
        Page<EmergencyAlert> alerts = safetyService.getEmergencyAlerts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @GetMapping("/alerts/{id}")
    @Operation(summary = "Get alert by ID", description = "Get emergency alert details")
    public ResponseEntity<ApiResponse> getAlert(@PathVariable UUID id) {
        EmergencyAlert alert = safetyService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success("Alert retrieved successfully", alert));
    }

    @GetMapping("/alerts/active")
    @Operation(summary = "Get active alerts", description = "Get all active emergency alerts")
    public ResponseEntity<ApiResponse> getActiveAlerts() {
        List<EmergencyAlert> alerts = safetyService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.success("Active alerts retrieved", alerts));
    }

    @PostMapping("/alerts/{id}/acknowledge")
    @Operation(summary = "Acknowledge alert", description = "Acknowledge an emergency alert")
    public ResponseEntity<ApiResponse> acknowledgeAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {

        EmergencyAlert acknowledged = safetyService.acknowledgeAlert(id, user);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged", acknowledged));
    }

    @PostMapping("/alerts/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Resolve alert", description = "Resolve an emergency alert")
    public ResponseEntity<ApiResponse> resolveAlert(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @RequestParam(required = false) String notes) {

        EmergencyAlert resolved = safetyService.resolveAlert(id, notes, user);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved", resolved));
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics")
    @Operation(summary = "Get safety statistics", description = "Get safety module statistics")
    public ResponseEntity<ApiResponse> getStatistics() {
        Map<String, Object> stats = safetyService.getSafetyStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }
}
