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
@Table(name = "complaints")
@ToString(callSuper = true)
public class Complaint extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // ACADEMIC, INFRASTRUCTURE, STAFF, BULLYING, FEES, OTHER

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT

    @Column(name = "filed_by_id", length = 50)
    private String filedById;

    @Column(name = "filed_by_name", length = 200)
    private String filedByName;

    @Column(name = "filed_by_role", length = 50)
    private String filedByRole;

    @Column(name = "assigned_to_id", length = 50)
    private String assignedToId;

    @Column(name = "assigned_to_name", length = 200)
    private String assignedToName;

    @Column(name = "resolution", length = 2000)
    private String resolution;

    @Column(name = "filed_date")
    private LocalDate filedDate;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
