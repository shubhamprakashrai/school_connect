package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
@ToString(callSuper = true)
public class AuditLog extends BaseEntity {

    @Column(name = "action", nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, EXPORT, IMPORT

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // STUDENT, TEACHER, FEE, EXAM, etc.

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_name", length = 200)
    private String userName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "action_timestamp")
    private LocalDateTime actionTimestamp;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
