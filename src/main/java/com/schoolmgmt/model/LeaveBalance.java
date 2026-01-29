package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_balances",
       indexes = {
           @Index(name = "idx_leave_bal_tenant", columnList = "tenant_id"),
           @Index(name = "idx_leave_bal_user", columnList = "user_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_leave_balance",
                           columnNames = {"tenant_id", "user_id", "leave_type_id", "academic_year"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LeaveBalance implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "total_allocated")
    @Builder.Default
    private Integer totalAllocated = 0;

    @Column(name = "used")
    @Builder.Default
    private Integer used = 0;

    @Column(name = "pending")
    @Builder.Default
    private Integer pending = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public int getRemaining() {
        return totalAllocated - used - pending;
    }
}
