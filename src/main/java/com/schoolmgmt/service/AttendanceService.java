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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public List<Attendance> markBulkAttendance(List<AttendanceMarkingRequest> requests) {
        return requests.stream()
                .map(this::markAttendance)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Attendance> getAllAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Attendance> getAttendanceByStudent(UUID studentId, Pageable pageable) {
        return attendanceRepository.findByStudentId(studentId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Attendance> getAttendanceByClass(UUID classId, Pageable pageable) {
        return attendanceRepository.findByTeacherClass_SectionId(classId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Attendance> getAttendanceByDate(LocalDate date, Pageable pageable) {
        return attendanceRepository.findByAttendanceDate(date, pageable);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDateRange(UUID studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAttendancePercentage(UUID studentId, LocalDate startDate, LocalDate endDate) {
        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Attendance> attendanceRecords = getAttendanceByDateRange(studentId, startDate, endDate);

        long totalDays = attendanceRecords.size();
        long presentDays = 0;
        long absentDays = 0;
        long lateDays = 0;

        // Single-pass iteration instead of 3 separate stream operations
        for (Attendance attendance : attendanceRecords) {
            switch (attendance.getStatus()) {
                case PRESENT -> presentDays++;
                case ABSENT -> absentDays++;
                case LATE -> lateDays++;
                default -> { /* HALF_DAY, EXCUSED - counted in total but not categorized here */ }
            }
        }

        double attendancePercentage = totalDays > 0 ? (double) presentDays / totalDays * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("studentId", studentId);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("totalDays", totalDays);
        stats.put("presentDays", presentDays);
        stats.put("absentDays", absentDays);
        stats.put("lateDays", lateDays);
        stats.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return stats;
    }

    @Transactional
    public Attendance updateAttendance(UUID id, AttendanceMarkingRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());
        attendance.setAttendanceDate(request.getAttendanceDate());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteAttendance(UUID id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
        
        attendanceRepository.delete(attendance);
    }
}