package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a section within a SchoolClass (e.g., Section 'A' of "Grade 10").
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"school_class_id", "name", "tenant_id"}, name = "uk_section_class_name_tenant")
})
@EqualsAndHashCode(callSuper = true, of = {"name", "schoolClass"})
@ToString(callSuper = true, of = {"name"})
public class Section extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name; // e.g., "A", "B", "Blue"

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "school_class_id", nullable = false)
    private UUID schoolClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", insertable = false, updatable = false)
    private SchoolClass schoolClass;

    /**
     * The teacher who is the primary class teacher for this specific section.
     */
    @OneToOne
    @JoinColumn(name = "class_teacher_id", referencedColumnName = "id")
    private Teacher classTeacher;
}