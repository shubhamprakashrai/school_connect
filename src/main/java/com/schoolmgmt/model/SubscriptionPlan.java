package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans",
       indexes = {
           @Index(name = "idx_sub_plan_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sub_plan_active", columnList = "is_active")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SubscriptionPlan {

    public enum BillingCycle {
        MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "per_student_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal perStudentPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 7;

    @Column(name = "read_only_period_days")
    @Builder.Default
    private Integer readOnlyPeriodDays = 7;

    @Column(name = "min_days_for_billing")
    @Builder.Default
    private Integer minDaysForBilling = 10;

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
