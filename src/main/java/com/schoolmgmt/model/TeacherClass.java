package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * TeacherClass entity representing teacher assignments to classes.
 */
@Entity
@Table(name = "teacher_classes",
       indexes = {
           @Index(name = "idx_teacher_class_teacher", columnList = "teacher_id, tenant_id"),
           @Index(name = "idx_teacher_class_class", columnList = "class_id, section_id, tenant_id"),
           @Index(name = "idx_teacher_class_active", columnList = "is_active, tenant_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"teacher_id", "class_id", "section_id", "subject", "tenant_id"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherClass extends BaseEntity {

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "subject", length = 100)
    private String subject; // Optional: for subject-specific assignments

    @Column(name = "is_class_teacher")
    @Builder.Default
    private Boolean isClassTeacher = false; // Is this teacher the main class teacher?

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "unassigned_date")
    private LocalDate unassignedDate;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "assigned_by")
    private UUID assignedBy; // Who assigned this teacher

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private Teacher teacher;

    // Business Methods
    public String getClassSection() {
        if (sectionId != null && !sectionId.isEmpty()) {
            return classId + "-" + sectionId;
        }
        return classId;
    }

    public boolean isCurrentlyActive() {
        return isActive && (unassignedDate == null || unassignedDate.isAfter(LocalDate.now()));
    }

    public void deactivate() {
        this.isActive = false;
        this.unassignedDate = LocalDate.now();
    }

    public void activate() {
        this.isActive = true;
        this.unassignedDate = null;
        if (this.assignedDate == null) {
            this.assignedDate = LocalDate.now();
        }
    }
}
