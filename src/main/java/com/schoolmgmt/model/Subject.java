package com.schoolmgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

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

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code; // e.g., "MATH101"

    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Mathematics"

    @Column(name = "description", length = 500)
    private String description;
}