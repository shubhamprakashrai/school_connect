package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assignment entity representing a homework, classwork, or project assignment.
 */
@Entity
@Table(name = "assignments",
       indexes = {
           @Index(name = "idx_assignment_tenant", columnList = "tenant_id"),
           @Index(name = "idx_assignment_class", columnList = "class_id"),
           @Index(name = "idx_assignment_teacher", columnList = "teacher_id"),
           @Index(name = "idx_assignment_subject", columnList = "subject_id"),
           @Index(name = "idx_assignment_due_date", columnList = "due_date"),
           @Index(name = "idx_assignment_status", columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Assignment implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "max_marks")
    @Builder.Default
    private Integer maxMarks = 100;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private AssignmentType type = AssignmentType.HOMEWORK;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum AssignmentStatus {
        DRAFT,
        PUBLISHED,
        CLOSED
    }

    public enum AssignmentType {
        HOMEWORK,
        CLASSWORK,
        PROJECT
    }
}
