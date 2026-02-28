package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StudentAttendance entity representing daily attendance records for students.
 * Attendance is taken based on the tenant calendar (working days only).
 */
@Entity
@Table(name = "student_attendance",
       indexes = {
           @Index(name = "idx_attendance_student_date", columnList = "student_id, attendance_date, tenant_id", unique = true),
           @Index(name = "idx_attendance_section_date", columnList = "section_id, attendance_date, tenant_id"),
           @Index(name = "idx_attendance_date", columnList = "attendance_date, tenant_id"),
           @Index(name = "idx_attendance_status", columnList = "status, tenant_id"),
           @Index(name = "idx_attendance_academic_year", columnList = "academic_year_id, tenant_id")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class StudentAttendance extends BaseEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column(name = "remarks", length = 500)
    private String remarks; // Reason for absence, late arrival, etc.

    @Column(name = "marked_by", nullable = false, length = 100)
    private String markedBy; // Teacher/User ID who marked attendance (username)

    @Column(name = "marked_at")
    @Builder.Default
    private LocalDateTime markedAt = LocalDateTime.now();

    @Column(name = "attendance_updated_by", length = 100)
    private String attendanceUpdatedBy; // Who updated the record (for audit)

    @Column(name = "attendance_updated_at")
    private LocalDateTime attendanceUpdatedAt;

    @Column(name = "is_half_day")
    @Builder.Default
    private Boolean isHalfDay = false;

    @Column(name = "half_day_type", length = 20)
    @Enumerated(EnumType.STRING)
    private HalfDayType halfDayType; // MORNING or AFTERNOON

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", insertable = false, updatable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by", referencedColumnName = "username", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    private User markedByUser;

    // Business Methods
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
    }

    public boolean isAbsent() {
        return status == AttendanceStatus.ABSENT;
    }

    public boolean isOnLeave() {
        return status == AttendanceStatus.LEAVE;
    }

    public void markUpdated(UUID updaterId) {
        this.attendanceUpdatedBy = updaterId != null ? updaterId.toString() : null;
        this.attendanceUpdatedAt = LocalDateTime.now();
    }

    public enum AttendanceStatus {
        PRESENT,      // Student was present
        ABSENT,       // Student was absent
        LATE,         // Student arrived late
        LEAVE,        // Student was on approved leave
        HALF_DAY,     // Student attended half day
        EXCUSED       // Absence excused (medical, etc.)
    }

    public enum HalfDayType {
        MORNING,      // Present in morning, absent in afternoon
        AFTERNOON     // Absent in morning, present in afternoon
    }
}
