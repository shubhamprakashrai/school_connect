package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * CalendarEvent entity representing tenant-specific calendar events.
 * Used to define holidays, working days, special events, and academic schedule.
 */
@Entity
@Table(name = "calendar_events",
       indexes = {
           @Index(name = "idx_calendar_tenant_date", columnList = "tenant_id, event_date"),
           @Index(name = "idx_calendar_type", columnList = "event_type, tenant_id"),
           @Index(name = "idx_calendar_academic_year", columnList = "academic_year_id, tenant_id"),
           @Index(name = "idx_calendar_date_range", columnList = "event_date, end_date")
       },
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"tenant_id", "event_date", "event_type"}, name = "uk_calendar_event")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class CalendarEvent extends BaseEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "end_date")
    private LocalDate endDate; // For multi-day events

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "event_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @ToString.Include
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_working_day")
    @Builder.Default
    private Boolean isWorkingDay = true;

    @Column(name = "academic_year_id")
    private UUID academicYearId;

    @Column(name = "applicable_sections")
    private String applicableSections; // Comma-separated section IDs, null means all sections

    @Column(name = "half_day")
    @Builder.Default
    private Boolean halfDay = false;

    @Column(name = "holiday_type", length = 50)
    private String holidayType; // NATIONAL, REGIONAL, RELIGIOUS, SCHOOL_EVENT, etc.

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", insertable = false, updatable = false)
    private AcademicYear academicYear;

    // Business Methods
    public boolean isMultiDayEvent() {
        return endDate != null && !endDate.equals(eventDate);
    }

    public boolean isApplicableToSection(UUID sectionId) {
        if (applicableSections == null || applicableSections.isEmpty()) {
            return true; // Applies to all sections
        }
        return applicableSections.contains(sectionId.toString());
    }

    public boolean isHoliday() {
        return !Boolean.TRUE.equals(isWorkingDay);
    }

    public enum EventType {
        WORKING_DAY,      // Regular school day
        HOLIDAY,          // Non-working day
        HALF_DAY,         // Half working day
        EXAM,             // Examination day
        EVENT,            // School event (may be working or non-working)
        TEACHER_MEETING,  // Staff meeting (students may have holiday)
        SPORTS_DAY,       // Sports/cultural events
        PARENT_TEACHER_MEETING
    }
}
