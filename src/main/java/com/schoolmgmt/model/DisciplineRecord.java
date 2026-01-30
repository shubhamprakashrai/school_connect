package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discipline_records")
@ToString(callSuper = true)
public class DisciplineRecord extends BaseEntity {

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType; // MISCONDUCT, BULLYING, VANDALISM, TRUANCY, CHEATING, OTHER

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "action_taken", length = 500)
    private String actionTaken;

    @Column(name = "reported_by", length = 200)
    private String reportedBy;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "OPEN"; // OPEN, UNDER_REVIEW, RESOLVED, CLOSED

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
