package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for incident reports (safety incidents in school).
 */
@Entity
@Table(name = "incident_reports", indexes = {
    @Index(name = "idx_incident_tenant", columnList = "tenant_id"),
    @Index(name = "idx_incident_status", columnList = "status"),
    @Index(name = "idx_incident_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private IncidentCategory category;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "reported_by_role", length = 50)
    private String reportedByRole;

    @Column(name = "status", length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.REPORTED;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;  // JSON array of attachment URLs

    @Column(name = "witnesses", columnDefinition = "TEXT")
    private String witnesses;  // JSON array of witness names

    @Column(name = "students_involved", columnDefinition = "TEXT")
    private String studentsInvolved;  // JSON array of student IDs

    public enum IncidentCategory {
        BULLYING,
        MISCONDUCT,
        ACCIDENT,
        HEALTH,
        PROPERTY_DAMAGE,
        THEFT,
        VIOLENCE,
        HARASSMENT,
        OTHER
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum IncidentStatus {
        REPORTED,
        UNDER_REVIEW,
        INVESTIGATING,
        RESOLVED,
        CLOSED,
        ESCALATED
    }
}
