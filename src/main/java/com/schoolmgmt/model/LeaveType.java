package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_types",
       indexes = {
           @Index(name = "idx_leave_type_tenant", columnList = "tenant_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LeaveType implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 50)
    private String name; // SICK, CASUAL, EARNED, MATERNITY, PATERNITY, UNPAID

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "max_days_per_year")
    @Builder.Default
    private Integer maxDaysPerYear = 12;

    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = true;

    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column(name = "applicable_roles", length = 200)
    private String applicableRoles; // comma-separated: TEACHER,STAFF

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
