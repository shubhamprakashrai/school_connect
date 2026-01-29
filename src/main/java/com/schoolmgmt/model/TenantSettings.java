package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantSettings entity for storing school-specific configuration.
 * Each tenant (school) has their own customizable settings.
 */
@Entity
@Table(name = "tenant_settings",
       indexes = {
           @Index(name = "idx_tenant_settings_tenant_id", columnList = "tenant_id", unique = true)
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TenantSettings implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    // ===== Branding Settings =====
    @Column(name = "display_name", length = 200)
    private String displayName; // How the school name appears in the app

    @Column(name = "tagline", length = 500)
    private String tagline; // School motto or tagline

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "primary_color", length = 7)
    @Builder.Default
    private String primaryColor = "#1E3A5F"; // Default dark blue

    @Column(name = "secondary_color", length = 7)
    @Builder.Default
    private String secondaryColor = "#4CAF50"; // Default green

    @Column(name = "accent_color", length = 7)
    @Builder.Default
    private String accentColor = "#FFC107"; // Default amber

    // ===== Academic Settings =====
    @Column(name = "academic_year_start")
    private LocalDate academicYearStart;

    @Column(name = "academic_year_end")
    private LocalDate academicYearEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_system", length = 20)
    @Builder.Default
    private GradingSystem gradingSystem = GradingSystem.PERCENTAGE;

    @Column(name = "passing_percentage")
    @Builder.Default
    private Integer passingPercentage = 33;

    @Column(name = "default_working_days", length = 50)
    @Builder.Default
    private String defaultWorkingDays = "MON,TUE,WED,THU,FRI"; // Comma-separated days

    @Column(name = "school_start_time", length = 10)
    @Builder.Default
    private String schoolStartTime = "08:00";

    @Column(name = "school_end_time", length = 10)
    @Builder.Default
    private String schoolEndTime = "14:00";

    // ===== Feature Flags =====
    @Column(name = "attendance_enabled")
    @Builder.Default
    private Boolean attendanceEnabled = true;

    @Column(name = "fees_enabled")
    @Builder.Default
    private Boolean feesEnabled = true;

    @Column(name = "exams_enabled")
    @Builder.Default
    private Boolean examsEnabled = true;

    @Column(name = "timetable_enabled")
    @Builder.Default
    private Boolean timetableEnabled = true;

    @Column(name = "library_enabled")
    @Builder.Default
    private Boolean libraryEnabled = false;

    @Column(name = "transport_enabled")
    @Builder.Default
    private Boolean transportEnabled = false;

    @Column(name = "hostel_enabled")
    @Builder.Default
    private Boolean hostelEnabled = false;

    @Column(name = "parent_portal_enabled")
    @Builder.Default
    private Boolean parentPortalEnabled = true;

    @Column(name = "student_portal_enabled")
    @Builder.Default
    private Boolean studentPortalEnabled = true;

    @Column(name = "sms_notifications_enabled")
    @Builder.Default
    private Boolean smsNotificationsEnabled = false;

    @Column(name = "email_notifications_enabled")
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "push_notifications_enabled")
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

    // ===== Locale Settings =====
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "dd/MM/yyyy";

    @Column(name = "time_format", length = 10)
    @Builder.Default
    private String timeFormat = "HH:mm";

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    // ===== Contact Settings =====
    @Column(name = "support_email", length = 100)
    private String supportEmail;

    @Column(name = "support_phone", length = 20)
    private String supportPhone;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    // ===== Timestamps =====
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    // ===== Enums =====
    public enum GradingSystem {
        PERCENTAGE,   // 0-100%
        CGPA,         // 0-10 CGPA
        GPA,          // 0-4 GPA
        LETTER_GRADE, // A, B, C, D, F
        MARKS         // Raw marks
    }
}
