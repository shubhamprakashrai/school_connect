package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a subject taught in the school (e.g., "Mathematics", "History").
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"}, name = "uk_subject_tenant_code")
})
@EqualsAndHashCode(callSuper = true, of = {"code"})
@ToString(callSuper = true, of = {"name", "code"})
public class Subject extends BaseEntity {

    @Column(name = "code", nullable = false, length = 20)
    private String code; // e.g., "MATH101"

    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Mathematics"

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    @Builder.Default
    private SubjectType type = SubjectType.CORE;

    @Column(name = "credit_hours")
    private Integer creditHours;

    @Column(name = "max_marks")
    private Integer maxMarks;

    @Column(name = "passing_marks")
    private Integer passingMarks;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "subject_prerequisites", joinColumns = @JoinColumn(name = "subject_id"))
    @Column(name = "prerequisite")
    @Builder.Default
    private List<String> prerequisites = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "subject_learning_objectives", joinColumns = @JoinColumn(name = "subject_id"))
    @Column(name = "objective", length = 500)
    @Builder.Default
    private List<String> learningObjectives = new ArrayList<>();

    // Many-to-Many relationship with SchoolClass
    @ManyToMany(mappedBy = "subjects", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private Set<SchoolClass> classes = new HashSet<>();

    // One-to-Many relationship with TeacherSubject
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeacherSubject> teacherSubjects = new HashSet<>();

    /**
     * Subject types enum
     */
    public enum SubjectType {
        CORE,           // Mandatory subjects
        ELECTIVE,       // Optional subjects
        EXTRA_CURRICULAR // Sports, Arts, etc.
    }

    // Helper methods
    public void setActive(Boolean active) {
        this.isActive = active != null ? active : true;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }
}