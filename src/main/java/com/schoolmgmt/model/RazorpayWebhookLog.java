package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "razorpay_webhook_logs",
       indexes = {
           @Index(name = "idx_rwl_event_id", columnList = "event_id"),
           @Index(name = "idx_rwl_order_id", columnList = "razorpay_order_id"),
           @Index(name = "idx_rwl_payment_id", columnList = "razorpay_payment_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RazorpayWebhookLog {

    public enum ProcessingStatus {
        RECEIVED, PROCESSED, FAILED, SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", length = 20)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.RECEIVED;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
