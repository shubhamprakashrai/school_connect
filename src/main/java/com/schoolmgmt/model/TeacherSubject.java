package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity representing the many-to-many relationship between Teachers and Subjects.
 * This allows a teacher to teach multiple subjects and a subject to be taught by multiple teachers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teacher_subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "subject_id", "tenant_id"}, 
                     name = "uk_teacher_subject_tenant")
})
@EqualsAndHashCode(callSuper = true, of = {"teacherId", "subjectId"})
@ToString(callSuper = true, of = {"teacherId", "subjectId", "isActive"})
public class TeacherSubject extends BaseEntity {

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "notes", length = 500)
    private String notes;

    // Lazy relationships to avoid circular dependencies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;
}