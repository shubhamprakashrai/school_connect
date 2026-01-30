package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Album entity representing a photo album in the school gallery.
 */
@Entity
@Table(name = "albums",
       indexes = {
           @Index(name = "idx_album_tenant", columnList = "tenant_id"),
           @Index(name = "idx_album_category", columnList = "category"),
           @Index(name = "idx_album_published", columnList = "is_published"),
           @Index(name = "idx_album_event_date", columnList = "event_date"),
           @Index(name = "idx_album_created_by", columnList = "created_by")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Album implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    @Builder.Default
    private AlbumCategory category = AlbumCategory.OTHER;

    @Column(name = "photo_count")
    @Builder.Default
    private Integer photoCount = 0;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum AlbumCategory {
        ACADEMIC,
        SPORTS,
        CULTURAL,
        OTHER
    }
}
