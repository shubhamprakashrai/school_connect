package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Book entity representing a library book in the school system.
 */
@Entity
@Table(name = "books",
       indexes = {
           @Index(name = "idx_book_tenant", columnList = "tenant_id"),
           @Index(name = "idx_book_isbn", columnList = "isbn"),
           @Index(name = "idx_book_category", columnList = "category"),
           @Index(name = "idx_book_title", columnList = "title"),
           @Index(name = "idx_book_author", columnList = "author")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Book implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "author", nullable = false, length = 300)
    private String author;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "total_copies", nullable = false)
    @Builder.Default
    private Integer totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    @Builder.Default
    private Integer availableCopies = 1;

    @Column(name = "publisher", length = 300)
    private String publisher;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "location", length = 100)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
