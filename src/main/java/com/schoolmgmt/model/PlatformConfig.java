package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing platform-wide configuration.
 * Managed by Super Admin for global settings.
 */
@Entity
@Table(name = "platform_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    // Branding
    @Column(name = "app_name", length = 100)
    @Builder.Default
    private String appName = "School Connect";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "primary_color", length = 20)
    @Builder.Default
    private String primaryColor = "#1976D2";

    @Column(name = "secondary_color", length = 20)
    @Builder.Default
    private String secondaryColor = "#424242";

    @Column(name = "accent_color", length = 20)
    @Builder.Default
    private String accentColor = "#FF9800";

    // Contact
    @Column(name = "support_email", length = 100)
    private String supportEmail;

    @Column(name = "support_phone", length = 20)
    private String supportPhone;

    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    // Legal Links
    @Column(name = "terms_url", length = 500)
    private String termsUrl;

    @Column(name = "privacy_url", length = 500)
    private String privacyUrl;

    // Feature Flags (JSON string)
    @Column(name = "feature_flags", columnDefinition = "TEXT")
    private String featureFlags;

    // Social Links (JSON string)
    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    // Maintenance Mode
    @Column(name = "maintenance_mode")
    @Builder.Default
    private Boolean maintenanceMode = false;

    @Column(name = "maintenance_message", length = 500)
    private String maintenanceMessage;

    @Column(name = "maintenance_end_time")
    private LocalDateTime maintenanceEndTime;

    // App Versions
    @Column(name = "min_android_version", length = 20)
    private String minAndroidVersion;

    @Column(name = "min_ios_version", length = 20)
    private String minIosVersion;

    @Column(name = "latest_android_version", length = 20)
    private String latestAndroidVersion;

    @Column(name = "latest_ios_version", length = 20)
    private String latestIosVersion;

    @Column(name = "force_update")
    @Builder.Default
    private Boolean forceUpdate = false;

    @Column(name = "update_message", length = 500)
    private String updateMessage;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
