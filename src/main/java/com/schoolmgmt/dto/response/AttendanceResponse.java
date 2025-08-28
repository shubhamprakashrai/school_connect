package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance response")
public class AttendanceResponse {

    private String id;
    private String studentId;
    private String studentName;
    private String rollNumber;
    private String classId;
    private String sectionId;
    private LocalDate attendanceDate;
    private String status;
    private String session;
    private String subject;
    private String markedByTeacherId;
    private String markedByTeacherName;
    private LocalTime markedAt;
    private String remarks;
}
