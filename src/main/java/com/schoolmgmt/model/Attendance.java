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
           // Index for finding all attendance records for a student on a given date.
           @Index(name = "idx_att_student_date", columnList = "student_id, attendance_date, tenant_id"),
           // Index for finding all attendance records for a specific class session on a given date.
           @Index(name = "idx_att_assignment_date", columnList = "teacher_class_id, attendance_date, tenant_id")
       },
       uniqueConstraints = {
           // A student can have only one attendance status for a specific class session on a given day.
           @UniqueConstraint(columnNames = {"student_id", "teacher_class_id", "attendance_date", "tenant_id"}, name = "uk_student_class_attendance")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor

@AllArgsConstructor
public class Attendance extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "teacher_class_id", nullable = false)
    private UUID teacherClassId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "marked_at", nullable = false)
    private LocalTime markedAt;

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_class_id", insertable = false, updatable = false)
    private TeacherClass teacherClass;

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
}
