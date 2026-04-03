package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "school_subscriptions",
       indexes = {
           @Index(name = "idx_school_sub_tenant", columnList = "tenant_id", unique = true),
           @Index(name = "idx_school_sub_status", columnList = "status"),
           @Index(name = "idx_school_sub_expires", columnList = "expires_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SchoolSubscription {

    public enum SubscriptionStatus {
        ACTIVE, GRACE, READ_ONLY, SUSPENDED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "current_cycle_start")
    private LocalDate currentCycleStart;

    @Column(name = "current_cycle_end")
    private LocalDate currentCycleEnd;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
