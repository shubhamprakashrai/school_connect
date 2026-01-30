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
@Table(name = "health_records")
@ToString(callSuper = true)
public class HealthRecord extends BaseEntity {

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType; // VACCINATION, ALLERGY, MEDICAL_CONDITION, CHECKUP, MEDICATION

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "doctor_name", length = 200)
    private String doctorName;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "prescription", length = 1000)
    private String prescription;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;

    @Column(name = "severity", length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
