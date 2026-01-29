package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.TenantSettings;
import com.schoolmgmt.model.TenantSettings.GradingSystem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant settings response")
public class TenantSettingsResponse {

    @Schema(description = "Settings ID")
    private UUID id;

    @Schema(description = "Tenant ID")
    private String tenantId;

    // Branding
    @Schema(description = "School display name")
    private String displayName;

    @Schema(description = "School tagline")
    private String tagline;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Favicon URL")
    private String faviconUrl;

    @Schema(description = "Primary color")
    private String primaryColor;

    @Schema(description = "Secondary color")
    private String secondaryColor;

    @Schema(description = "Accent color")
    private String accentColor;

    // Academic Settings
    @Schema(description = "Academic year start date")
    private LocalDate academicYearStart;

    @Schema(description = "Academic year end date")
    private LocalDate academicYearEnd;

    @Schema(description = "Grading system")
    private GradingSystem gradingSystem;

    @Schema(description = "Passing percentage")
    private Integer passingPercentage;

    @Schema(description = "Working days")
    private String defaultWorkingDays;

    @Schema(description = "School start time")
    private String schoolStartTime;

    @Schema(description = "School end time")
    private String schoolEndTime;

    // Feature Flags
    @Schema(description = "Attendance module enabled")
    private Boolean attendanceEnabled;

    @Schema(description = "Fees module enabled")
    private Boolean feesEnabled;

    @Schema(description = "Exams module enabled")
    private Boolean examsEnabled;

    @Schema(description = "Timetable module enabled")
    private Boolean timetableEnabled;

    @Schema(description = "Library module enabled")
    private Boolean libraryEnabled;

    @Schema(description = "Transport module enabled")
    private Boolean transportEnabled;

    @Schema(description = "Hostel module enabled")
    private Boolean hostelEnabled;

    @Schema(description = "Parent portal enabled")
    private Boolean parentPortalEnabled;

    @Schema(description = "Student portal enabled")
    private Boolean studentPortalEnabled;

    @Schema(description = "SMS notifications enabled")
    private Boolean smsNotificationsEnabled;

    @Schema(description = "Email notifications enabled")
    private Boolean emailNotificationsEnabled;

    @Schema(description = "Push notifications enabled")
    private Boolean pushNotificationsEnabled;

    // Locale Settings
    @Schema(description = "Timezone")
    private String timezone;

    @Schema(description = "Date format")
    private String dateFormat;

    @Schema(description = "Time format")
    private String timeFormat;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Language")
    private String language;

    // Contact Settings
    @Schema(description = "Support email")
    private String supportEmail;

    @Schema(description = "Support phone")
    private String supportPhone;

    @Schema(description = "Emergency contact")
    private String emergencyContact;

    // Timestamps
    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static TenantSettingsResponse fromEntity(TenantSettings entity) {
        if (entity == null) {
            return null;
        }

        return TenantSettingsResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .displayName(entity.getDisplayName())
                .tagline(entity.getTagline())
                .logoUrl(entity.getLogoUrl())
                .faviconUrl(entity.getFaviconUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .accentColor(entity.getAccentColor())
                .academicYearStart(entity.getAcademicYearStart())
                .academicYearEnd(entity.getAcademicYearEnd())
                .gradingSystem(entity.getGradingSystem())
                .passingPercentage(entity.getPassingPercentage())
                .defaultWorkingDays(entity.getDefaultWorkingDays())
                .schoolStartTime(entity.getSchoolStartTime())
                .schoolEndTime(entity.getSchoolEndTime())
                .attendanceEnabled(entity.getAttendanceEnabled())
                .feesEnabled(entity.getFeesEnabled())
                .examsEnabled(entity.getExamsEnabled())
                .timetableEnabled(entity.getTimetableEnabled())
                .libraryEnabled(entity.getLibraryEnabled())
                .transportEnabled(entity.getTransportEnabled())
                .hostelEnabled(entity.getHostelEnabled())
                .parentPortalEnabled(entity.getParentPortalEnabled())
                .studentPortalEnabled(entity.getStudentPortalEnabled())
                .smsNotificationsEnabled(entity.getSmsNotificationsEnabled())
                .emailNotificationsEnabled(entity.getEmailNotificationsEnabled())
                .pushNotificationsEnabled(entity.getPushNotificationsEnabled())
                .timezone(entity.getTimezone())
                .dateFormat(entity.getDateFormat())
                .timeFormat(entity.getTimeFormat())
                .currency(entity.getCurrency())
                .language(entity.getLanguage())
                .supportEmail(entity.getSupportEmail())
                .supportPhone(entity.getSupportPhone())
                .emergencyContact(entity.getEmergencyContact())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
