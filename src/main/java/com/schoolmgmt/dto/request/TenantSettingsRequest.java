package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.TenantSettings.GradingSystem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update tenant settings")
public class TenantSettingsRequest {

    // Branding
    @Size(max = 200, message = "Display name must not exceed 200 characters")
    @Schema(description = "School display name", example = "Greenwood International School")
    private String displayName;

    @Size(max = 500, message = "Tagline must not exceed 500 characters")
    @Schema(description = "School tagline or motto", example = "Excellence in Education")
    private String tagline;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    @Schema(description = "School logo URL")
    private String logoUrl;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format. Use hex format like #1E3A5F")
    @Schema(description = "Primary brand color", example = "#1E3A5F")
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
    @Schema(description = "Secondary brand color", example = "#4CAF50")
    private String secondaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
    @Schema(description = "Accent color", example = "#FFC107")
    private String accentColor;

    // Academic Settings
    @Schema(description = "Academic year start date")
    private LocalDate academicYearStart;

    @Schema(description = "Academic year end date")
    private LocalDate academicYearEnd;

    @Schema(description = "Grading system type")
    private GradingSystem gradingSystem;

    @Schema(description = "Minimum passing percentage", example = "33")
    private Integer passingPercentage;

    @Schema(description = "Working days (comma-separated)", example = "MON,TUE,WED,THU,FRI")
    private String defaultWorkingDays;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format. Use HH:mm")
    @Schema(description = "School start time", example = "08:00")
    private String schoolStartTime;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format")
    @Schema(description = "School end time", example = "14:00")
    private String schoolEndTime;

    // Feature Flags
    @Schema(description = "Enable attendance module")
    private Boolean attendanceEnabled;

    @Schema(description = "Enable fees module")
    private Boolean feesEnabled;

    @Schema(description = "Enable exams module")
    private Boolean examsEnabled;

    @Schema(description = "Enable timetable module")
    private Boolean timetableEnabled;

    @Schema(description = "Enable library module")
    private Boolean libraryEnabled;

    @Schema(description = "Enable transport module")
    private Boolean transportEnabled;

    @Schema(description = "Enable hostel module")
    private Boolean hostelEnabled;

    @Schema(description = "Enable parent portal")
    private Boolean parentPortalEnabled;

    @Schema(description = "Enable student portal")
    private Boolean studentPortalEnabled;

    @Schema(description = "Enable SMS notifications")
    private Boolean smsNotificationsEnabled;

    @Schema(description = "Enable email notifications")
    private Boolean emailNotificationsEnabled;

    @Schema(description = "Enable push notifications")
    private Boolean pushNotificationsEnabled;

    // Locale Settings
    @Schema(description = "Timezone", example = "Asia/Kolkata")
    private String timezone;

    @Schema(description = "Date format", example = "dd/MM/yyyy")
    private String dateFormat;

    @Schema(description = "Time format", example = "HH:mm")
    private String timeFormat;

    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Schema(description = "Currency code", example = "INR")
    private String currency;

    @Size(min = 2, max = 10, message = "Language code must be 2-10 characters")
    @Schema(description = "Language code", example = "en")
    private String language;

    // Contact Settings
    @Schema(description = "Support email")
    private String supportEmail;

    @Schema(description = "Support phone")
    private String supportPhone;

    @Schema(description = "Emergency contact number")
    private String emergencyContact;
}
