package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.AttendanceMarkingRequest;
import com.schoolmgmt.exception.BadRequestException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Attendance;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.model.TeacherClass;
import com.schoolmgmt.repository.AttendanceRepository;
import com.schoolmgmt.repository.StudentRepository;
import com.schoolmgmt.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherClassRepository teacherClassRepository;

    @Transactional
    public Attendance markAttendance(AttendanceMarkingRequest request) {
        // 1. Fetch the core entities.
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        TeacherClass teacherClass = teacherClassRepository.findById(request.getTeacherClassId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherClass", "id", request.getTeacherClassId()));

        // 2. Important Validation: Ensure the student belongs to the section of the class assignment.
        if (!student.getSectionId().equals(teacherClass.getSectionId())) {
            throw new BadRequestException("Student does not belong to the section of this class.");
        }

        // 3. Create and save the attendance record.
        Attendance attendance = Attendance.builder()
                .studentId(student.getId())
                .teacherClassId(teacherClass.getId())
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .markedAt(LocalTime.now())
                .build();

        return attendanceRepository.save(attendance);
    }
}