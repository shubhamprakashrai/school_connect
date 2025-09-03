package com.schoolmgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents an academic year or session in the school (e.g., "2024-2025").
 * This acts as a master record for managing school years.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "academic_years", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "name"}, name = "uk_academic_year_tenant_name")
})
public class AcademicYear extends BaseEntity {

    @Column(name = "name", nullable = false, length = 20)
    private String name; // e.g., "2024-2025"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false; // Is this the currently active academic year?
}