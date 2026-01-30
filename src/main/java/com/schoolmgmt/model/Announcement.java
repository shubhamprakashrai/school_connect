package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Announcement entity representing a school-wide or targeted announcement.
 */
@Entity
@Table(name = "announcements",
       indexes = {
           @Index(name = "idx_announcement_tenant", columnList = "tenant_id"),
           @Index(name = "idx_announcement_author", columnList = "author_id"),
           @Index(name = "idx_announcement_priority", columnList = "priority"),
           @Index(name = "idx_announcement_published", columnList = "is_published"),
           @Index(name = "idx_announcement_published_at", columnList = "published_at"),
           @Index(name = "idx_announcement_expires_at", columnList = "expires_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Announcement implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_name", length = 200)
    private String authorName;

    @ElementCollection
    @CollectionTable(name = "announcement_target_roles", joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "role")
    @Builder.Default
    private List<String> targetRoles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "announcement_target_classes", joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "class_id")
    @Builder.Default
    private List<String> targetClassIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private AnnouncementPriority priority = AnnouncementPriority.MEDIUM;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ElementCollection
    @CollectionTable(name = "announcement_attachments", joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "attachment_url", length = 500)
    @Builder.Default
    private List<String> attachmentUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum AnnouncementPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
