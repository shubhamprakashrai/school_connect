package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AssignmentSubmission entity representing a student's submission for an assignment.
 */
@Entity
@Table(name = "assignment_submissions",
       indexes = {
           @Index(name = "idx_submission_tenant", columnList = "tenant_id"),
           @Index(name = "idx_submission_assignment", columnList = "assignment_id"),
           @Index(name = "idx_submission_student", columnList = "student_id"),
           @Index(name = "idx_submission_assignment_student", columnList = "assignment_id, student_id", unique = true),
           @Index(name = "idx_submission_status", columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AssignmentSubmission implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "marks_obtained")
    private Double marksObtained;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubmissionStatus {
        PENDING,
        SUBMITTED,
        GRADED,
        LATE
    }
}
