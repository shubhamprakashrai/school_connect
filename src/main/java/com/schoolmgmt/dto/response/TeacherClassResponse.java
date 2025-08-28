package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Teacher class assignment response")
public class TeacherClassResponse {

    private String id;
    private String teacherId;
    private String teacherName;
    private String classId;
    private String sectionId;
    private String classSection;
    private String subject;
    private Boolean isClassTeacher;
    private Boolean isActive;
    private LocalDate assignedDate;
    private LocalDate unassignedDate;
    private String academicYear;
    private String assignedBy;
    private String remarks;
}
