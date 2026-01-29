package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an academic calendar event such as a holiday, exam, meeting, or activity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "academic_events")
public class AcademicEvent extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_all_day", nullable = false)
    @Builder.Default
    private Boolean isAllDay = true;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "color", length = 10)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 20)
    @Builder.Default
    private TargetAudience targetAudience = TargetAudience.ALL;

    @Column(name = "class_id")
    private UUID classId;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    public enum EventType {
        HOLIDAY,
        EXAM,
        MEETING,
        ACTIVITY,
        CUSTOM
    }

    public enum TargetAudience {
        ALL,
        STUDENTS,
        TEACHERS,
        PARENTS
    }
}
