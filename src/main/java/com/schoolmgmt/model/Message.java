package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message entity representing a single message in a conversation.
 */
@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_message_tenant", columnList = "tenant_id"),
           @Index(name = "idx_message_sender", columnList = "sender_id"),
           @Index(name = "idx_message_recipient", columnList = "recipient_id"),
           @Index(name = "idx_message_conversation", columnList = "conversation_id"),
           @Index(name = "idx_message_sent_at", columnList = "sent_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Message implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "sender_name", nullable = false, length = 200)
    private String senderName;

    @Column(name = "sender_role", length = 50)
    private String senderRole;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}
