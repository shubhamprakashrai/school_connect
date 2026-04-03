package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_invoices",
       indexes = {
           @Index(name = "idx_sub_inv_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sub_inv_status", columnList = "status"),
           @Index(name = "idx_sub_inv_razorpay", columnList = "razorpay_order_id"),
           @Index(name = "idx_sub_inv_number", columnList = "invoice_number", unique = true)
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SubscriptionInvoice {

    public enum InvoiceStatus {
        DRAFT, PENDING, PAID, OVERDUE, CANCELLED
    }

    public enum PaymentMode {
        RAZORPAY, MANUAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SchoolSubscription subscription;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "per_student_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal perStudentAmount;

    @Column(name = "billable_student_count", nullable = false)
    private Integer billableStudentCount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "adjustment_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(name = "adjustment_note", length = 500)
    private String adjustmentNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Column(name = "manual_payment_ref", length = 200)
    private String manualPaymentRef;

    @Column(name = "manual_payment_note", length = 500)
    private String manualPaymentNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
