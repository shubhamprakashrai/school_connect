package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ExamType entity representing different types of exams
 * (e.g., Unit Test, Mid-Term, Final, Practical).
 */
@Entity
@Table(name = "exam_types",
       indexes = {
           @Index(name = "idx_exam_type_tenant", columnList = "tenant_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ExamType implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "weightage")
    @Builder.Default
    private Double weightage = 0.0; // Weightage in final grade calculation (0-100)

    @Column(name = "max_marks")
    @Builder.Default
    private Integer maxMarks = 100;

    @Column(name = "passing_marks")
    @Builder.Default
    private Integer passingMarks = 33;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
