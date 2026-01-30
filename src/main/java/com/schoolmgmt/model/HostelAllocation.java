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
@Table(name = "hostel_allocations")
@ToString(callSuper = true)
public class HostelAllocation extends BaseEntity {

    @Column(name = "hostel_id", nullable = false, length = 50)
    private String hostelId;

    @Column(name = "room_id", nullable = false, length = 50)
    private String roomId;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "student_name", length = 200)
    private String studentName;

    @Column(name = "allocation_date")
    private LocalDate allocationDate;

    @Column(name = "vacating_date")
    private LocalDate vacatingDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, VACATED, TRANSFERRED

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
