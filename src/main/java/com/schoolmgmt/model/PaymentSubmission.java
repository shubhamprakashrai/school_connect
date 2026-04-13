package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Parent/student-uploaded proof of an offline payment. An approver
 * (anyone with FEES_APPROVE) reviews the screenshot and either
 * approves (which creates a FeePayment downstream) or rejects with
 * a reason.
 *
 * Partial payments are supported — {@code amount} can be less than the
 * outstanding fee balance.
 */
@Entity
@Table(name = "payment_submissions", indexes = {
    @Index(name = "idx_ps_tenant", columnList = "tenant_id"),
    @Index(name = "idx_ps_status", columnList = "tenant_id, status"),
    @Index(name = "idx_ps_student", columnList = "student_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentSubmission extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /** Optional — links to a specific FeeStructure / outstanding fee row. */
    @Column(name = "fee_id")
    private UUID feeId;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "screenshot_url", nullable = false, length = 500)
    private String screenshotUrl;

    @Column(name = "remarks", length = 500)
    private String remarks;          // submitter-supplied note

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public enum SubmissionStatus { PENDING, APPROVED, REJECTED }
}
