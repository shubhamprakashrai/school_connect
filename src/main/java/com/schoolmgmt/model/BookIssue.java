package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BookIssue entity representing a book issued to a student.
 */
@Entity
@Table(name = "book_issues",
       indexes = {
           @Index(name = "idx_book_issue_tenant", columnList = "tenant_id"),
           @Index(name = "idx_book_issue_book", columnList = "book_id"),
           @Index(name = "idx_book_issue_student", columnList = "student_id"),
           @Index(name = "idx_book_issue_status", columnList = "status"),
           @Index(name = "idx_book_issue_due_date", columnList = "due_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class BookIssue implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookIssueStatus status = BookIssueStatus.ISSUED;

    @Column(name = "fine_amount")
    @Builder.Default
    private Double fineAmount = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum BookIssueStatus {
        ISSUED,
        RETURNED,
        OVERDUE
    }
}
