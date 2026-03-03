package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a class or grade level in the school (e.g., "Grade 10", "Class VI").
 * This acts as a master list for all available classes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "school_classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"}, name = "uk_school_class_tenant_code")
})
@EqualsAndHashCode(callSuper = true, of = {"code"})
@ToString(callSuper = true, of = {"name", "code"})
public class SchoolClass extends BaseEntity {

    /**
     * The unique code for the class, e.g., "10", "VI", "NURSERY". This is the business identifier.
     */
    @Column(name = "code", nullable = true, length = 20)
    private String code;
    
    /**
     * The class identifier generated based on tenant, e.g., "SM00005CL01".
     */
    @Column(name = "class_identifier", nullable = false, unique = true, length = 50)
    private String classIdentifier;

    /**
     * The full display name of the class, e.g., "Grade 10", "Class VI".
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * An optional description for the class.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * The sections that belong to this class.
     */

    @OneToMany(mappedBy = "schoolClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Section> sections = new HashSet<>(); // <-- initialize here
    
    // Many-to-Many relationship with Subject
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "school_class_subjects",
        joinColumns = @JoinColumn(name = "school_class_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private Set<Subject> subjects = new HashSet<>();
}

