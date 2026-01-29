package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for counseling referrals.
 */
@Entity
@Table(name = "counseling_referrals", indexes = {
    @Index(name = "idx_counseling_tenant", columnList = "tenant_id"),
    @Index(name = "idx_counseling_student", columnList = "student_id"),
    @Index(name = "idx_counseling_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselingReferral extends BaseEntity {

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "class_info", length = 100)
    private String classInfo;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "urgency", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Urgency urgency;

    @Column(name = "referred_by", length = 100)
    private String referredBy;

    @Column(name = "referred_by_role", length = 50)
    private String referredByRole;

    @Column(name = "status", length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(name = "counselor_assigned", length = 100)
    private String counselorAssigned;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "session_notes", columnDefinition = "TEXT")
    private String sessionNotes;

    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum Urgency {
        LOW,
        MEDIUM,
        HIGH,
        IMMEDIATE
    }

    public enum ReferralStatus {
        PENDING,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        FOLLOW_UP
    }
}
