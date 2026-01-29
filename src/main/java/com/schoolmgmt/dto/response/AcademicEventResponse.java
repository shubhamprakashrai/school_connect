package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Academic event response")
public class AcademicEventResponse {
    private String id;
    private String title;
    private String description;
    private String eventType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isAllDay;
    private Boolean isRecurring;
    private String color;
    private String targetAudience;
    private String classId;
    private String academicYear;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
