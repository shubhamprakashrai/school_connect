package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_templates",
       indexes = {
           @Index(name = "idx_notif_template_tenant", columnList = "tenant_id"),
           @Index(name = "idx_notif_template_type", columnList = "type")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class NotificationTemplate implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 30)
    private String type; // FEE_REMINDER, ATTENDANCE_ALERT, EXAM_NOTICE, ANNOUNCEMENT, LEAVE_STATUS, CUSTOM

    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    @Column(name = "body_template", nullable = false, length = 1000)
    private String bodyTemplate;

    @Column(name = "channel", length = 20)
    @Builder.Default
    private String channel = "PUSH"; // PUSH, EMAIL, SMS

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
