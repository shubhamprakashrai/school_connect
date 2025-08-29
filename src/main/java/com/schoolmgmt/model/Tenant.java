package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant entity representing a school organization in the multi-tenant system.
 * Each tenant has isolated data and configuration.
 */
@Entity
@Table(name = "tenants", 
       indexes = {
           @Index(name = "idx_tenant_identifier", columnList = "identifier", unique = true),
           @Index(name = "idx_tenant_subdomain", columnList = "subdomain", unique = true),
           @Index(name = "idx_tenant_status", columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identifier", unique = true, nullable = false, length = 7)
    private String identifier; // Unique tenant identifier (e.g., "SM00001")


    @Column(name = "name", nullable = false, length = 200)
    private String name; // School name

    @Column(name = "subdomain", unique = true, nullable = false, length = 50)
    private String subdomain; // Subdomain for tenant access (e.g., "greenwood" for greenwood.schoolmgmt.com)

    @Column(name = "schema_name", unique = true, nullable = false, length = 63)
    private String schemaName; // Database schema name for this tenant

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    // Contact Information
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Configuration
    @Column(name = "config", columnDefinition = "TEXT")
    private String configuration; // JSON configuration for tenant-specific settings

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website", length = 200)
    private String website;

    // Limits and Quotas
    @Column(name = "max_students")
    @Builder.Default
    private Integer maxStudents = 500;

    @Column(name = "max_teachers")
    @Builder.Default
    private Integer maxTeachers = 50;

    @Column(name = "max_storage_gb")
    @Builder.Default
    private Integer maxStorageGb = 10;

    @Column(name = "current_students")
    @Builder.Default
    private Integer currentStudents = 0;

    @Column(name = "current_teachers")
    @Builder.Default
    private Integer currentTeachers = 0;

    @Column(name = "current_storage_mb")
    @Builder.Default
    private Integer currentStorageMb = 0;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Metadata
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;


    // Business Methods
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public boolean canAddStudent() {
        return currentStudents < maxStudents;
    }

    public boolean canAddTeacher() {
        return currentTeachers < maxTeachers;
    }

    public boolean hasStorageSpace(int requiredMb) {
        return (currentStorageMb + requiredMb) <= (maxStorageGb * 1024);
    }

    // Enums
    public enum TenantStatus {
        PENDING,    // Initial state after creation
        ACTIVE,     // Fully operational
        SUSPENDED,  // Temporarily disabled
        DELETED     // Soft deleted
    }

    public enum SubscriptionPlan {
        TRIAL,      // 30-day trial
        BASIC,      // Basic features
        STANDARD,   // Standard features
        PREMIUM,    // Premium features
        ENTERPRISE  // Enterprise features
    }
}
