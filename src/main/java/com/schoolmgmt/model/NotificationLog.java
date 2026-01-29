package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_logs",
       indexes = {
           @Index(name = "idx_notif_log_tenant", columnList = "tenant_id"),
           @Index(name = "idx_notif_log_recipient", columnList = "recipient_user_id"),
           @Index(name = "idx_notif_log_read", columnList = "is_read"),
           @Index(name = "idx_notif_log_created", columnList = "created_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class NotificationLog implements TenantAware {

    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED, READ
    }

    public enum NotificationType {
        FEE_REMINDER, ATTENDANCE_ALERT, EXAM_NOTICE, ANNOUNCEMENT,
        LEAVE_STATUS, TIMETABLE_CHANGE, RESULT_PUBLISHED, CUSTOM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Column(name = "notification_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "recipient_user_id", length = 50)
    private String recipientUserId;

    @Column(name = "recipient_role", length = 30)
    private String recipientRole;

    @Column(name = "recipient_class_id")
    private UUID recipientClassId;

    @Column(name = "sender_user_id", length = 50)
    private String senderUserId;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "data_payload", length = 2000)
    private String dataPayload; // JSON string for navigation/extra data

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
