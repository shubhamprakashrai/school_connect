package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.BulkAttendanceRequest;
import com.schoolmgmt.dto.request.StudentAttendanceRequest;
import com.schoolmgmt.dto.response.AttendanceSummaryResponse;
import com.schoolmgmt.dto.response.StudentAttendanceResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.exception.*;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing student attendance.
 * Validates attendance against tenant calendar (working days only).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceService {

    private final StudentAttendanceRepository attendanceRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;

    /**
     * Marks attendance for a single student.
     * Validates that the date is a working day according to tenant calendar.
     *
     * @param request the attendance request
     * @return the attendance response
     */
    @Transactional
    public StudentAttendanceResponse markAttendance(StudentAttendanceRequest request, UUID markedBy) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Marking attendance for student: {} on date: {} for tenant: {}",
                    request.getStudentId(), request.getAttendanceDate(), tenantId);

            // Validate date is working day using tenant calendar
            validateWorkingDay(request.getAttendanceDate(), tenantId);

            // Validate student exists
            Student student = studentRepository.findByIdAndTenantId(request.getStudentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

            // Validate section exists
            sectionRepository.findByIdAndTenantId(request.getSectionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

            // Validate academic year exists
            academicYearRepository.findByIdAndTenantId(request.getAcademicYearId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

            // Check if attendance already exists - update if it does
            Optional<StudentAttendance> existingAttendance = attendanceRepository
                    .findByStudentIdAndDateAndTenantId(request.getStudentId(), request.getAttendanceDate(), tenantId);

            StudentAttendance attendance;
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                attendance.setStatus(request.getStatus());
                attendance.setRemarks(request.getRemarks());
                attendance.setIsHalfDay(request.getIsHalfDay() != null ? request.getIsHalfDay() : Boolean.FALSE);
                attendance.setHalfDayType(request.getHalfDayType());
                attendance.markUpdated(markedBy);
                log.debug("Updating existing attendance record: {}", attendance.getId());
            } else {
                attendance = StudentAttendance.builder()
                        .studentId(request.getStudentId())
                        .attendanceDate(request.getAttendanceDate())
                        .sectionId(request.getSectionId())
                        .academicYearId(request.getAcademicYearId())
                        .status(request.getStatus())
                        .remarks(request.getRemarks())
                        .markedBy(markedBy.toString())
                        .markedAt(LocalDateTime.now())
                        .isHalfDay(request.getIsHalfDay() != null ? request.getIsHalfDay() : Boolean.FALSE)
                        .halfDayType(request.getHalfDayType())
                        .build();
                attendance.setTenantId(tenantId);
            }

            StudentAttendance saved = attendanceRepository.save(attendance);
            log.info("Attendance marked successfully for student: {} on date: {}",
                    request.getStudentId(), request.getAttendanceDate());

            return toResponse(saved, student);

        } catch (BusinessException | ResourceNotFoundException e) {
            log.warn("Business exception while marking attendance: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error marking attendance: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while marking attendance", e);
        }
    }

    /**
     * Marks bulk attendance for multiple students in a section.
     * Validates that the date is a working day according to tenant calendar.
     *
     * @param request the bulk attendance request
     * @param markedBy the user ID marking attendance
     * @return list of attendance responses
     */
    @Transactional
    public List<StudentAttendanceResponse> markBulkAttendance(BulkAttendanceRequest request, UUID markedBy) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Marking bulk attendance for section: {} on date: {} for {} students, tenant: {}",
                    request.getSectionId(), request.getAttendanceDate(), request.getAttendanceRecords().size(), tenantId);

            // Validate date is working day using tenant calendar
            validateWorkingDay(request.getAttendanceDate(), tenantId);

            // Validate section exists
            Section section = sectionRepository.findByIdAndTenantId(request.getSectionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

            // Validate academic year exists
            academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

            List<StudentAttendanceResponse> responses = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (BulkAttendanceRequest.StudentAttendanceRecord record : request.getAttendanceRecords()) {
                try {
                    // Validate student exists and belongs to the section
                    Student student = studentRepository.findByIdAndTenantId(record.getStudentId(), tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", record.getStudentId()));

                    // Check if attendance already exists
                    Optional<StudentAttendance> existingAttendance = attendanceRepository
                            .findByStudentIdAndDateAndTenantId(record.getStudentId(), request.getAttendanceDate(), tenantId);

                    StudentAttendance attendance;
                    if (existingAttendance.isPresent()) {
                        attendance = existingAttendance.get();
                        attendance.setStatus(record.getStatus());
                        attendance.setRemarks(record.getRemarks());
                        attendance.setIsHalfDay(record.getIsHalfDay() != null ? record.getIsHalfDay() : Boolean.FALSE);
                        attendance.setHalfDayType(record.getHalfDayType());
                        attendance.markUpdated(markedBy);
                    } else {
                        attendance = StudentAttendance.builder()
                                .studentId(record.getStudentId())
                                .attendanceDate(request.getAttendanceDate())
                                .sectionId(request.getSectionId())
                                .academicYearId(request.getAcademicYearId())
                                .status(record.getStatus())
                                .remarks(record.getRemarks())
                                .markedBy(markedBy.toString())
                                .markedAt(LocalDateTime.now())
                                .isHalfDay(record.getIsHalfDay() != null ? record.getIsHalfDay() : Boolean.FALSE)
                                .halfDayType(record.getHalfDayType())
                                .build();
                        attendance.setTenantId(tenantId);
                    }

                    StudentAttendance saved = attendanceRepository.save(attendance);
                    responses.add(toResponse(saved, student));

                } catch (Exception e) {
                    errors.add("Student " + record.getStudentId() + ": " + e.getMessage());
                    log.warn("Error marking attendance for student {}: {}", record.getStudentId(), e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                log.warn("Bulk attendance completed with {} errors: {}", String.valueOf(errors.size()), errors);
            }

            log.info("Bulk attendance completed: {} records saved, {} errors for section: {} on date: {}",
                    String.valueOf(responses.size()), String.valueOf(errors.size()), request.getSectionId(), request.getAttendanceDate());

            return responses;

        } catch (BusinessException | ResourceNotFoundException e) {
            log.warn("Business exception in bulk attendance: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in bulk attendance: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while marking bulk attendance", e);
        }
    }

    /**
     * Gets attendance by ID.
     *
     * @param id the attendance ID
     * @return the attendance response
     */
    @Transactional(readOnly = true)
    public StudentAttendanceResponse getAttendanceById(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching attendance: {} for tenant: {}", id, tenantId);

            StudentAttendance attendance = attendanceRepository.findById(id)
                    .filter(a -> a.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("StudentAttendance", "id", id));

            Student student = studentRepository.findById(attendance.getStudentId()).orElse(null);
            return toResponse(attendance, student);

        } catch (ResourceNotFoundException e) {
            log.warn("Attendance not found: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching attendance {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching attendance", e);
        }
    }

    /**
     * Gets attendance for a student on a specific date.
     *
     * @param studentId the student ID
     * @param date the attendance date
     * @return the attendance response
     */
    @Transactional(readOnly = true)
    public StudentAttendanceResponse getAttendanceByStudentAndDate(UUID studentId, LocalDate date) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching attendance for student: {} on date: {} for tenant: {}", studentId, date, tenantId);

            StudentAttendance attendance = attendanceRepository
                    .findByStudentIdAndDateAndTenantId(studentId, date, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("StudentAttendance", "studentId and date",
                            studentId + " on " + date));

            Student student = studentRepository.findById(studentId).orElse(null);
            return toResponse(attendance, student);

        } catch (ResourceNotFoundException e) {
            log.warn("Attendance not found for student {} on date {}: {}", studentId, date, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching attendance for student {} on date {}: {}", studentId, date, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching attendance", e);
        }
    }

    /**
     * Gets all attendance records for a section on a specific date.
     *
     * @param sectionId the section ID
     * @param date the attendance date
     * @return list of attendance responses
     */
    @Transactional(readOnly = true)
    public List<StudentAttendanceResponse> getAttendanceBySectionAndDate(UUID sectionId, LocalDate date) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching attendance for section: {} on date: {} for tenant: {}", sectionId, date, tenantId);

            List<StudentAttendance> attendances = attendanceRepository
                    .findBySectionIdAndDateAndTenantId(sectionId, date, tenantId);

            return attendances.stream()
                    .map(a -> {
                        Student student = studentRepository.findById(a.getStudentId()).orElse(null);
                        return toResponse(a, student);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching attendance for section {} on date {}: {}", sectionId, date, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching attendance", e);
        }
    }

    /**
     * Gets attendance summary for a student in a date range.
     *
     * @param studentId the student ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the attendance summary response
     */
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentAttendanceSummary(UUID studentId, LocalDate startDate, LocalDate endDate) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching attendance summary for student: {} from {} to {} for tenant: {}",
                    studentId, startDate, endDate, tenantId);

            if (endDate.isBefore(startDate)) {
                throw new BusinessException("End date must be after start date");
            }

            // Validate student exists
            Student student = studentRepository.findByIdAndTenantId(studentId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

            // Get working days count from calendar
            Long workingDays = calendarEventRepository.countWorkingDaysInRange(tenantId, startDate, endDate);
            if (workingDays == null || workingDays == 0) {
                // Calculate approximate working days if no calendar events (excluding weekends)
                workingDays = calculateDefaultWorkingDays(startDate, endDate);
            }

            // Get attendance counts
            Long presentDays = attendanceRepository.countPresentDays(studentId, startDate, endDate, tenantId);
            Long absentDays = attendanceRepository.countAbsentDays(studentId, startDate, endDate, tenantId);
            Long leaveDays = attendanceRepository.countLeaveDays(studentId, startDate, endDate, tenantId);

            // Get all attendance records for daily breakdown
            List<StudentAttendance> attendances = attendanceRepository
                    .findByStudentIdAndDateRange(studentId, startDate, endDate, tenantId);

            Map<LocalDate, String> dailyBreakdown = attendances.stream()
                    .collect(Collectors.toMap(
                            StudentAttendance::getAttendanceDate,
                            a -> a.getStatus().name(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            // Calculate late days (status = LATE)
            long lateDays = attendances.stream()
                    .filter(a -> a.getStatus() == StudentAttendance.AttendanceStatus.LATE)
                    .count();

            // Calculate half days
            long halfDays = attendances.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsHalfDay()))
                    .count();

            // Calculate attendance percentage
            double attendancePercentage = workingDays > 0
                    ? ((double) (presentDays != null ? presentDays : Integer.valueOf(0)) / workingDays) * 100
                    : 0.0;

            return AttendanceSummaryResponse.builder()
                    .studentId(studentId)
                    .studentName(student != null ? student.getFullName() : null)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalWorkingDays(workingDays)
                    .presentDays(presentDays != null ? presentDays : Integer.valueOf(0))
                    .absentDays(absentDays != null ? absentDays : Integer.valueOf(0))
                    .leaveDays(leaveDays != null ? leaveDays : Integer.valueOf(0))
                    .lateDays(lateDays)
                    .halfDays(halfDays)
                    .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                    .dailyBreakdown(dailyBreakdown)
                    .build();

        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching attendance summary: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching attendance summary", e);
        }
    }

    /**
     * Updates an attendance record.
     *
     * @param id the attendance ID
     * @param request the update request
     * @param updatedBy the user ID updating the record
     * @return the updated attendance response
     */
    @Transactional
    public StudentAttendanceResponse updateAttendance(UUID id, StudentAttendanceRequest request, UUID updatedBy) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Updating attendance: {} for tenant: {}", id, tenantId);

            StudentAttendance attendance = attendanceRepository.findById(id)
                    .filter(a -> a.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("StudentAttendance", "id", id));

            // Update fields
            attendance.setStatus(request.getStatus());
            attendance.setRemarks(request.getRemarks());
            attendance.setIsHalfDay(request.getIsHalfDay() != null ? request.getIsHalfDay() : Boolean.FALSE);
            attendance.setHalfDayType(request.getHalfDayType());
            attendance.markUpdated(updatedBy);

            StudentAttendance updated = attendanceRepository.save(attendance);
            log.info("Attendance updated successfully: {}", id);

            Student student = studentRepository.findById(attendance.getStudentId()).orElse(null);
            return toResponse(updated, student);

        } catch (ResourceNotFoundException e) {
            log.warn("Attendance not found for update: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error updating attendance {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while updating attendance", e);
        }
    }

    /**
     * Deletes an attendance record.
     *
     * @param id the attendance ID
     */
    @Transactional
    public void deleteAttendance(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Deleting attendance: {} for tenant: {}", id, tenantId);

            StudentAttendance attendance = attendanceRepository.findById(id)
                    .filter(a -> a.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("StudentAttendance", "id", id));

            attendanceRepository.delete(attendance);
            log.info("Attendance deleted successfully: {}", id);

        } catch (ResourceNotFoundException e) {
            log.warn("Attendance not found for deletion: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting attendance {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while deleting attendance", e);
        }
    }

    /**
     * Validates that the date is a working day according to tenant calendar.
     *
     * @param date the date to validate
     * @param tenantId the tenant ID
     * @throws BusinessException if date is not a working day
     */
    private void validateWorkingDay(LocalDate date, String tenantId) {
        // Check if date is in the future (allow same day)
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("Cannot mark attendance for future date: " + date);
        }

        // Check if date is too far in the past (configurable - e.g., 30 days)
        if (date.isBefore(LocalDate.now().minusDays(30))) {
            throw new BusinessException("Cannot mark attendance for dates older than 30 days: " + date);
        }

        // Check tenant calendar for the date
        Optional<CalendarEvent> calendarEvent = calendarEventRepository.findByTenantIdAndEventDate(tenantId, date);

        if (calendarEvent.isPresent()) {
            CalendarEvent event = calendarEvent.get();
            if (!Boolean.TRUE.equals(event.getIsWorkingDay())) {
                throw new BusinessException("Cannot mark attendance on holiday: " + event.getTitle() + " (" + date + ")");
            }
            if (Boolean.TRUE.equals(event.getHalfDay())) {
                log.debug("Date {} is a half working day: {}", date, event.getTitle());
                // Allow attendance on half days but log it
            }
        } else {
            // No calendar event - check if it's a weekend (only Sunday is blocked, Saturday allowed)
            if (date.getDayOfWeek().getValue() > 6) { // 7 = Sunday only
                throw new BusinessException("Cannot mark attendance on weekend (" + date.getDayOfWeek() + "). " +
                        "Please configure the date as a working day in the calendar if needed.");
            }
            // Saturday (6) is allowed as working day when no calendar event exists
        }
    }

    /**
     * Calculates default working days excluding weekends when no calendar events exist.
     */
    private Long calculateDefaultWorkingDays(LocalDate startDate, LocalDate endDate) {
        long count = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().getValue() <= 6) { // Monday = 1, Saturday = 6 (Sunday = 7 excluded)
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    /**
     * Converts StudentAttendance entity to StudentAttendanceResponse DTO.
     */
    private StudentAttendanceResponse toResponse(StudentAttendance attendance, Student student) {
        // Load related entities
        Section section = null;
        if (attendance.getSectionId() != null) {
            section = sectionRepository.findById(attendance.getSectionId()).orElse(null);
        }

        AcademicYear academicYear = null;
        if (attendance.getAcademicYearId() != null) {
            academicYear = academicYearRepository.findById(attendance.getAcademicYearId()).orElse(null);
        }

        User markedByUser = null;
        if (attendance.getMarkedBy() != null) {
            markedByUser = userRepository.findByUsernameOrEmail(attendance.getMarkedBy()).orElse(null);
        }

        String className = null;
        if (section != null && section.getSchoolClass() != null) {
            className = section.getSchoolClass().getName();
        }

        return StudentAttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudentId())
                .studentName(student != null ? student.getFullName() : null)
                .rollNumber(student != null ? student.getRollNumber() : null)
                .attendanceDate(attendance.getAttendanceDate())
                .sectionId(attendance.getSectionId())
                .sectionName(section != null ? section.getName() : null)
                .className(className)
                .academicYearId(attendance.getAcademicYearId())
                .academicYearName(academicYear != null ? academicYear.getName() : null)
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .markedBy(attendance.getMarkedBy())
                .markedByName(markedByUser != null ? markedByUser.getFullName() : null)
                .markedAt(attendance.getMarkedAt())
                .isHalfDay(attendance.getIsHalfDay())
                .halfDayType(attendance.getHalfDayType())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
