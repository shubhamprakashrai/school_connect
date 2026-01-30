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
@Table(name = "performance_reviews")
@ToString(callSuper = true)
public class PerformanceReview extends BaseEntity {

    @Column(name = "staff_id", nullable = false, length = 50)
    private String staffId;

    @Column(name = "staff_name", length = 200)
    private String staffName;

    @Column(name = "reviewer_id", length = 50)
    private String reviewerId;

    @Column(name = "reviewer_name", length = 200)
    private String reviewerName;

    @Column(name = "review_period", nullable = false, length = 50)
    private String reviewPeriod; // Q1_2025, ANNUAL_2025, etc.

    @Column(name = "overall_rating")
    private Integer overallRating; // 1-5

    @Column(name = "teaching_rating")
    private Integer teachingRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "punctuality_rating")
    private Integer punctualityRating;

    @Column(name = "professional_development_rating")
    private Integer professionalDevelopmentRating;

    @Column(name = "strengths", length = 1000)
    private String strengths;

    @Column(name = "areas_for_improvement", length = 1000)
    private String areasForImprovement;

    @Column(name = "goals", length = 1000)
    private String goals;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, SUBMITTED, REVIEWED, ACKNOWLEDGED

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
