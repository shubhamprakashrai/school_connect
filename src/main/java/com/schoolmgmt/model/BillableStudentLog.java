package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "billable_student_logs",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_billable_student_snapshot",
                             columnNames = {"tenant_id", "student_id", "snapshot_date"})
       },
       indexes = {
           @Index(name = "idx_bsl_tenant_date", columnList = "tenant_id, snapshot_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class BillableStudentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
