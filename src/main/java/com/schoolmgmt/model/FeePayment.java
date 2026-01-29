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
@Table(name = "fee_payments",
       indexes = {
           @Index(name = "idx_fee_payment_tenant", columnList = "tenant_id"),
           @Index(name = "idx_fee_payment_student", columnList = "student_id"),
           @Index(name = "idx_fee_payment_date", columnList = "payment_date")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FeePayment implements TenantAware {

    public enum PaymentStatus {
        PENDING, PAID, PARTIAL, OVERDUE, CANCELLED, REFUNDED
    }

    public enum PaymentMode {
        CASH, CHEQUE, ONLINE, BANK_TRANSFER, UPI, CARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "late_fee_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFeeAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "collected_by", length = 100)
    private String collectedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void calculateBalance() {
        if (totalAmount != null && amountPaid != null) {
            this.balanceAmount = totalAmount.subtract(amountPaid);
            if (balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                this.paymentStatus = PaymentStatus.PAID;
                this.balanceAmount = BigDecimal.ZERO;
            } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                this.paymentStatus = PaymentStatus.PARTIAL;
            }
        }
    }
}
