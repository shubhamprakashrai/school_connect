package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "timetable_entries",
       indexes = {
           @Index(name = "idx_tt_tenant", columnList = "tenant_id"),
           @Index(name = "idx_tt_class", columnList = "class_id"),
           @Index(name = "idx_tt_teacher", columnList = "teacher_id"),
           @Index(name = "idx_tt_day_period", columnList = "day_of_week, period_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_tt_class_day_period",
                           columnNames = {"tenant_id", "class_id", "section", "day_of_week", "period_id"}),
           @UniqueConstraint(name = "uk_tt_teacher_day_period",
                           columnNames = {"tenant_id", "teacher_id", "day_of_week", "period_id"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TimetableEntry implements TenantAware {

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "period_id", nullable = false)
    private Period period;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "section", length = 10)
    private String section;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "subject_name", length = 100)
    private String subjectName;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "teacher_name", length = 200)
    private String teacherName;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
