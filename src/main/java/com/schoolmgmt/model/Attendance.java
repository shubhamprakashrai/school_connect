package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Attendance entity representing daily attendance records for students.
 */
@Entity
@Table(name = "attendance",
       indexes = {
           @Index(name = "idx_attendance_student_date", columnList = "student_id, attendance_date, tenant_id"),
           @Index(name = "idx_attendance_class_date", columnList = "class_id, attendance_date, tenant_id"),
           @Index(name = "idx_attendance_date", columnList = "attendance_date"),
           @Index(name = "idx_attendance_teacher", columnList = "marked_by_teacher_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"student_id", "attendance_date", "tenant_id"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Attendance extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "marked_by_teacher_id", nullable = false)
    private UUID markedByTeacherId;

    @Column(name = "marked_at", nullable = false)
    private LocalTime markedAt;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "subject", length = 100)
    private String subject; // For subject-wise attendance

    @Enumerated(EnumType.STRING)
    @Column(name = "session", length = 20)
    @Builder.Default
    private AttendanceSession session = AttendanceSession.FULL_DAY;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by_teacher_id", insertable = false, updatable = false)
    private Teacher teacher;

    // Business Methods
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT;
    }

    public boolean isAbsent() {
        return status == AttendanceStatus.ABSENT;
    }

    public boolean isLate() {
        return status == AttendanceStatus.LATE;
    }

    // Enums
    public enum AttendanceStatus {
        PRESENT("Present"),
        ABSENT("Absent"),
        LATE("Late"),
        HALF_DAY("Half Day"),
        EXCUSED("Excused Absence");

        private final String displayName;

        AttendanceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AttendanceSession {
        MORNING("Morning Session"),
        AFTERNOON("Afternoon Session"),
        FULL_DAY("Full Day");

        private final String displayName;

        AttendanceSession(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
