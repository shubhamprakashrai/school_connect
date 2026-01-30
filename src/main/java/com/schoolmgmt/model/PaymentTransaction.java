package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
@ToString(callSuper = true)
public class PaymentTransaction extends BaseEntity {

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // CASH, CARD, UPI, NET_BANKING, CHEQUE

    @Column(name = "payment_type", nullable = false, length = 30)
    private String paymentType; // TUITION_FEE, HOSTEL_FEE, TRANSPORT_FEE, EXAM_FEE, OTHER

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "gateway_reference", length = 200)
    private String gatewayReference;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED, REFUNDED

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
