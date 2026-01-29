package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for emergency alerts.
 */
@Entity
@Table(name = "emergency_alerts", indexes = {
    @Index(name = "idx_alert_tenant", columnList = "tenant_id"),
    @Index(name = "idx_alert_type", columnList = "alert_type"),
    @Index(name = "idx_alert_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlert extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "alert_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "triggered_by_role", length = 50)
    private String triggeredByRole;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "acknowledged")
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved")
    @Builder.Default
    private Boolean resolved = false;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "notifications_sent")
    @Builder.Default
    private Integer notificationsSent = 0;

    @Column(name = "target_audience", length = 100)
    private String targetAudience;  // ALL, TEACHERS, PARENTS, STUDENTS

    public enum AlertType {
        SOS,
        FIRE,
        LOCKDOWN,
        WEATHER,
        MEDICAL,
        EVACUATION,
        SECURITY,
        OTHER
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
