package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.CounselingReferral;
import com.schoolmgmt.model.EmergencyAlert;
import com.schoolmgmt.model.IncidentReport;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.CounselingReferralRepository;
import com.schoolmgmt.repository.EmergencyAlertRepository;
import com.schoolmgmt.repository.IncidentReportRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for safety-related operations: incidents, counseling, and emergency alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SafetyService {

    private final IncidentReportRepository incidentRepository;
    private final CounselingReferralRepository counselingRepository;
    private final EmergencyAlertRepository alertRepository;

    // ==================== INCIDENT REPORTS ====================

    public IncidentReport createIncidentReport(IncidentReport report, User user) {
        report.setReportedBy(user.getUserId());
        report.setReportedByRole(user.getPrimaryRole().name());
        report.setStatus(IncidentReport.IncidentStatus.REPORTED);

        IncidentReport saved = incidentRepository.save(report);
        log.info("Incident report created: {} by user: {}", saved.getId(), user.getUserId());
        return saved;
    }

    public Page<IncidentReport> getIncidentReports(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return incidentRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
    }

    public IncidentReport getIncidentById(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", "id", id));
    }

    public IncidentReport updateIncidentStatus(UUID id, IncidentReport.IncidentStatus status, String notes, User user) {
        IncidentReport incident = getIncidentById(id);
        incident.setStatus(status);

        if (status == IncidentReport.IncidentStatus.RESOLVED || status == IncidentReport.IncidentStatus.CLOSED) {
            incident.setResolvedAt(LocalDateTime.now());
            incident.setResolvedBy(user.getUserId());
            incident.setResolutionNotes(notes);
        }

        IncidentReport updated = incidentRepository.save(incident);
        log.info("Incident {} status updated to {} by {}", id, status, user.getUserId());
        return updated;
    }

    public List<IncidentReport> getIncidentsByStatus(IncidentReport.IncidentStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        return incidentRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }

    // ==================== COUNSELING REFERRALS ====================

    public CounselingReferral createCounselingReferral(CounselingReferral referral, User user) {
        referral.setReferredBy(user.getUserId());
        referral.setReferredByRole(user.getPrimaryRole().name());
        referral.setStatus(CounselingReferral.ReferralStatus.PENDING);

        CounselingReferral saved = counselingRepository.save(referral);
        log.info("Counseling referral created: {} for student: {} by user: {}",
                saved.getId(), saved.getStudentId(), user.getUserId());
        return saved;
    }

    public Page<CounselingReferral> getCounselingReferrals(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return counselingRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
    }

    public CounselingReferral getCounselingById(UUID id) {
        return counselingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CounselingReferral", "id", id));
    }

    public CounselingReferral updateCounselingStatus(UUID id, CounselingReferral.ReferralStatus status, String notes, User user) {
        CounselingReferral referral = getCounselingById(id);
        referral.setStatus(status);
        referral.setSessionNotes(notes);

        if (status == CounselingReferral.ReferralStatus.COMPLETED) {
            referral.setCompletedAt(LocalDateTime.now());
        }

        CounselingReferral updated = counselingRepository.save(referral);
        log.info("Counseling referral {} status updated to {} by {}", id, status, user.getUserId());
        return updated;
    }

    public List<CounselingReferral> getPendingReferrals() {
        String tenantId = TenantContext.getCurrentTenant();
        return counselingRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, CounselingReferral.ReferralStatus.PENDING);
    }

    public List<CounselingReferral> getStudentReferrals(String studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return counselingRepository.findByTenantIdAndStudentIdAndIsDeletedFalse(tenantId, studentId);
    }

    // ==================== EMERGENCY ALERTS ====================

    public EmergencyAlert triggerEmergencyAlert(EmergencyAlert alert, User user) {
        alert.setTriggeredBy(user.getUserId());
        alert.setTriggeredByRole(user.getPrimaryRole().name());
        alert.setIsActive(true);
        alert.setAcknowledged(false);
        alert.setResolved(false);

        EmergencyAlert saved = alertRepository.save(alert);
        log.warn("EMERGENCY ALERT triggered: {} - Type: {} by user: {}",
                saved.getId(), saved.getAlertType(), user.getUserId());

        // TODO: Send push notifications to all relevant users
        // notificationService.sendEmergencyAlert(saved);

        return saved;
    }

    public Page<EmergencyAlert> getEmergencyAlerts(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return alertRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
    }

    public EmergencyAlert getAlertById(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyAlert", "id", id));
    }

    public List<EmergencyAlert> getActiveAlerts() {
        String tenantId = TenantContext.getCurrentTenant();
        return alertRepository.findByTenantIdAndIsActiveAndIsDeletedFalse(tenantId, true);
    }

    public EmergencyAlert acknowledgeAlert(UUID id, User user) {
        EmergencyAlert alert = getAlertById(id);
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(user.getUserId());
        alert.setAcknowledgedAt(LocalDateTime.now());

        EmergencyAlert updated = alertRepository.save(alert);
        log.info("Emergency alert {} acknowledged by {}", id, user.getUserId());
        return updated;
    }

    public EmergencyAlert resolveAlert(UUID id, String notes, User user) {
        EmergencyAlert alert = getAlertById(id);
        alert.setIsActive(false);
        alert.setResolved(true);
        alert.setResolvedBy(user.getUserId());
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(notes);

        EmergencyAlert updated = alertRepository.save(alert);
        log.info("Emergency alert {} resolved by {}", id, user.getUserId());
        return updated;
    }

    // ==================== STATISTICS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> getSafetyStatistics() {
        String tenantId = TenantContext.getCurrentTenant();
        Map<String, Object> stats = new HashMap<>();

        // Incident stats - compute total from sum of known statuses instead of passing null
        long reportedCount = incidentRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, IncidentReport.IncidentStatus.REPORTED);
        long investigatingCount = incidentRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, IncidentReport.IncidentStatus.INVESTIGATING);
        long resolvedCount = incidentRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, IncidentReport.IncidentStatus.RESOLVED);
        long closedCount = incidentRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, IncidentReport.IncidentStatus.CLOSED);
        long totalIncidents = reportedCount + investigatingCount + resolvedCount + closedCount;

        stats.put("totalIncidents", totalIncidents);
        stats.put("openIncidents", reportedCount + investigatingCount);
        stats.put("highSeverityIncidents", incidentRepository.countByTenantIdAndSeverityAndIsDeletedFalse(tenantId, IncidentReport.Severity.HIGH)
                + incidentRepository.countByTenantIdAndSeverityAndIsDeletedFalse(tenantId, IncidentReport.Severity.CRITICAL));

        // Counseling stats
        stats.put("pendingReferrals", counselingRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, CounselingReferral.ReferralStatus.PENDING));
        stats.put("scheduledSessions", counselingRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, CounselingReferral.ReferralStatus.SCHEDULED));

        // Alert stats
        stats.put("activeAlerts", alertRepository.countByTenantIdAndIsActiveAndIsDeletedFalse(tenantId, true));

        return stats;
    }
}
