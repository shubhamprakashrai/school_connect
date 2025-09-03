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
           @Index(name = "idx_teacher_class_assignment", columnList = "section_id, subject_id, tenant_id"),
           @Index(name = "idx_teacher_class_active", columnList = "is_active, tenant_id")
       },
       uniqueConstraints = {
           // A teacher can only be assigned to a specific class/section/subject once per academic year.
           @UniqueConstraint(columnNames = {"teacher_id", "section_id", "subject_id", "academic_year_id", "tenant_id"}, name = "uk_teacher_assignment")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClass extends BaseEntity {

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "section_id")
    private UUID sectionId; // Nullable if a teacher is assigned to a class in general, not a specific section.

    @Column(name = "subject_id")
    private UUID subjectId; // Nullable if it's a class teacher assignment without a specific subject.

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "unassigned_date")
    private LocalDate unassignedDate;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "assigned_by")
    private UUID assignedBy; // Who assigned this teacher

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", insertable = false, updatable = false)
    private AcademicYear academicYear;

    // Business Methods
    public String getClassSection() {
        if (section == null || section.getSchoolClass() == null) {
            return "N/A";
        }
        // Access SchoolClass through the Section
        return section.getSchoolClass().getName() + " - " + section.getName();
    }

    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive) && (unassignedDate == null || !unassignedDate.isBefore(LocalDate.now()));
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
