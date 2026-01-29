package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ExamResult entity representing a student's result for an exam.
 */
@Entity
@Table(name = "exam_results",
       indexes = {
           @Index(name = "idx_result_tenant", columnList = "tenant_id"),
           @Index(name = "idx_result_exam", columnList = "exam_id"),
           @Index(name = "idx_result_student", columnList = "student_id"),
           @Index(name = "idx_result_exam_student", columnList = "exam_id, student_id", unique = true)
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ExamResult implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "grade", length = 5)
    private String grade;

    @Column(name = "rank")
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 20)
    @Builder.Default
    private ResultStatus resultStatus = ResultStatus.PENDING;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "is_absent")
    @Builder.Default
    private Boolean isAbsent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "entered_by", length = 100)
    private String enteredBy;

    @PrePersist
    @PreUpdate
    public void calculatePercentage() {
        if (marksObtained != null && maxMarks != null && maxMarks > 0) {
            this.percentage = (marksObtained / maxMarks) * 100;
            this.grade = calculateGrade(this.percentage);
            this.resultStatus = isAbsent ? ResultStatus.ABSENT
                    : (this.percentage >= 33 ? ResultStatus.PASS : ResultStatus.FAIL);
        }
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        if (percentage >= 33) return "E";
        return "F";
    }

    public enum ResultStatus {
        PENDING,
        PASS,
        FAIL,
        ABSENT,
        WITHHELD
    }
}
