package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_requests",
       indexes = {
           @Index(name = "idx_leave_req_tenant", columnList = "tenant_id"),
           @Index(name = "idx_leave_req_user", columnList = "user_id"),
           @Index(name = "idx_leave_req_status", columnList = "status"),
           @Index(name = "idx_leave_req_dates", columnList = "start_date,end_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LeaveRequest implements TenantAware {

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "user_name", length = 200)
    private String userName;

    @Column(name = "user_role", length = 30)
    private String userRole;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_by_name", length = 200)
    private String approvedByName;

    @Column(name = "approval_remarks", length = 500)
    private String approvalRemarks;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "is_half_day")
    @Builder.Default
    private Boolean isHalfDay = false;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateTotalDays() {
        if (startDate != null && endDate != null) {
            long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            if (Boolean.TRUE.equals(isHalfDay) && days == 1) {
                this.totalDays = 1; // half-day counts as 1 in integer, tracked via flag
            } else {
                this.totalDays = (int) days;
            }
        }
    }
}
