package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.AttendanceMarkingRequest;
import com.schoolmgmt.dto.request.BulkTeacherAttendanceRequest;
import com.schoolmgmt.dto.response.AttendanceResponse;
import com.schoolmgmt.dto.response.BulkTeacherAttendanceResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BulkTeacherAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final UserRepository userRepository;

    /**
     * Get bulk teacher attendance template with example data
     */
    public BulkTeacherAttendanceRequest getBulkTeacherAttendanceTemplate() {
        // Create example teacher attendance request
        BulkTeacherAttendanceRequest.TeacherAttendanceRecord exampleRecord = 
                BulkTeacherAttendanceRequest.TeacherAttendanceRecord.builder()
                        .teacherId(UUID.randomUUID())
                        .status(Attendance.AttendanceStatus.PRESENT)
                        .remarks("On time")
                        .checkInTime("08:30")
                        .checkOutTime("16:30")
                        .isHalfDay(false)
                        .build();

        return BulkTeacherAttendanceRequest.builder()
                .attendanceDate(java.time.LocalDate.now())
                .batchReference("TEMPLATE-EXAMPLE")
                .continueOnError(true)
                .attendanceRecords(Arrays.asList(exampleRecord))
                .build();
    }

    /**
     * Validate bulk teacher attendance data without creating records
     */
    public BulkTeacherAttendanceResponse validateBulkTeacherAttendance(BulkTeacherAttendanceRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        String batchReference = request.getBatchReference() != null ? 
            request.getBatchReference() : "VALIDATION-" + System.currentTimeMillis();
        
        log.info("Validating bulk teacher attendance for tenant: {}, batch: {}, total teachers: {}", 
                tenantId, batchReference, request.getAttendanceRecords().size());

        List<BulkTeacherAttendanceResponse.BulkError> errors = new ArrayList<>();
        Map<String, BulkTeacherAttendanceResponse.DepartmentSummary> departmentSummaries = new HashMap<>();

        // Validate all requests
        List<ValidatedTeacherAttendanceRequest> validatedRequests = validateBulkTeacherAttendanceRequests(
                request.getAttendanceRecords(), request.getAttendanceDate(), tenantId, errors);

        // Group by department for summary
        Map<String, List<ValidatedTeacherAttendanceRequest>> groupedTeachers = 
                groupTeachersByDepartment(validatedRequests);

        // Create department summaries
        for (Map.Entry<String, List<ValidatedTeacherAttendanceRequest>> entry : groupedTeachers.entrySet()) {
            String departmentKey = entry.getKey();
            List<ValidatedTeacherAttendanceRequest> departmentTeachers = entry.getValue();
            
            departmentSummaries.put(departmentKey, BulkTeacherAttendanceResponse.DepartmentSummary.builder()
                    .departmentName(departmentKey)
                    .totalRequested(departmentTeachers.size())
                    .successful(departmentTeachers.size())
                    .failed(0)
                    .build());
        }

        // Build validation response
        BulkTeacherAttendanceResponse response = BulkTeacherAttendanceResponse.builder()
                .batchReference(batchReference)
                .totalRequested(request.getAttendanceRecords().size())
                .successful(validatedRequests.size())
                .failed(errors.size())
                .createdAttendanceRecords(new ArrayList<>()) // No records created in validation
                .errors(errors)
                .departmentSummaries(departmentSummaries)
                .processedAt(LocalDateTime.now())
                .build();

        log.info("Bulk teacher attendance validation completed for batch {}: {} valid, {} invalid", 
                batchReference, response.getSuccessful(), response.getFailed());

        return response;
    }

    /**
     * Create bulk teacher attendance records
     */
    public BulkTeacherAttendanceResponse createBulkTeacherAttendance(BulkTeacherAttendanceRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        String batchReference = request.getBatchReference() != null ? 
            request.getBatchReference() : "BULK-TEACHER-" + System.currentTimeMillis();
        
        log.info("Starting bulk teacher attendance creation for tenant: {}, batch: {}, total teachers: {}", 
                tenantId, batchReference, request.getAttendanceRecords().size());

        List<BulkTeacherAttendanceResponse.BulkError> errors = new ArrayList<>();
        List<AttendanceResponse> createdAttendanceRecords = new ArrayList<>();
        Map<String, BulkTeacherAttendanceResponse.DepartmentSummary> departmentSummaries = new HashMap<>();

        // Validate all requests first
        List<ValidatedTeacherAttendanceRequest> validatedRequests = validateBulkTeacherAttendanceRequests(
                request.getAttendanceRecords(), request.getAttendanceDate(), tenantId, errors);

        if (validatedRequests.isEmpty()) {
            log.warn("All teacher attendance requests failed validation for batch: {}", batchReference);
            return buildErrorResponse(batchReference, request.getAttendanceRecords().size(), errors);
        }

        // Group teachers by department for efficient processing
        Map<String, List<ValidatedTeacherAttendanceRequest>> groupedTeachers = groupTeachersByDepartment(validatedRequests);

        // Process each department
        for (Map.Entry<String, List<ValidatedTeacherAttendanceRequest>> entry : groupedTeachers.entrySet()) {
            String departmentKey = entry.getKey();
            List<ValidatedTeacherAttendanceRequest> departmentTeachers = entry.getValue();
            
            try {
                processDepartmentTeachers(departmentTeachers, departmentKey, tenantId, 
                        createdAttendanceRecords, errors, departmentSummaries, request.getAttendanceDate());
            } catch (Exception e) {
                log.error("Error processing department {}: {}", departmentKey, e.getMessage());
                // Add errors for all teachers in this department
                for (ValidatedTeacherAttendanceRequest teacher : departmentTeachers) {
                    errors.add(BulkTeacherAttendanceResponse.BulkError.builder()
                            .rowIndex(teacher.getOriginalIndex())
                            .teacherId(teacher.getOriginalRequest().getTeacherId())
                            .teacherIdentifier(getTeacherIdentifier(teacher.getOriginalRequest()))
                            .errorMessage("Department processing failed: " + e.getMessage())
                            .errorType("DEPARTMENT_PROCESSING_ERROR")
                            .failedRequest(teacher.getOriginalRequest())
                            .build());
                }
            }
        }

        // Build final response
        BulkTeacherAttendanceResponse response = BulkTeacherAttendanceResponse.builder()
                .batchReference(batchReference)
                .totalRequested(request.getAttendanceRecords().size())
                .successful(createdAttendanceRecords.size())
                .failed(errors.size())
                .createdAttendanceRecords(createdAttendanceRecords)
                .errors(errors)
                .departmentSummaries(departmentSummaries)
                .processedAt(LocalDateTime.now())
                .build();

        log.info("Bulk teacher attendance creation completed for batch {}: {} successful, {} failed", 
                batchReference, response.getSuccessful(), response.getFailed());

        return response;
    }

    /**
     * Validate bulk teacher attendance requests
     */
    private List<ValidatedTeacherAttendanceRequest> validateBulkTeacherAttendanceRequests(
            List<BulkTeacherAttendanceRequest.TeacherAttendanceRecord> requests, LocalDate attendanceDate, String tenantId, 
            List<BulkTeacherAttendanceResponse.BulkError> errors) {
        
        List<ValidatedTeacherAttendanceRequest> validatedRequests = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            BulkTeacherAttendanceRequest.TeacherAttendanceRecord request = requests.get(i);
            
            try {
                validateTeacherAttendanceRequest(request, attendanceDate, tenantId);
                
                validatedRequests.add(ValidatedTeacherAttendanceRequest.builder()
                        .originalRequest(request)
                        .originalIndex(i)
                        .build());
                        
            } catch (Exception e) {
                errors.add(BulkTeacherAttendanceResponse.BulkError.builder()
                        .rowIndex(i)
                        .teacherId(request.getTeacherId())
                        .errorMessage(e.getMessage())
                        .errorType("VALIDATION_ERROR")
                        .failedRequest(request)
                        .teacherIdentifier(getTeacherIdentifier(request))
                        .build());
            }
        }
        
        return validatedRequests;
    }

    /**
     * Validate individual teacher attendance request
     */
    private void validateTeacherAttendanceRequest(BulkTeacherAttendanceRequest.TeacherAttendanceRecord request, LocalDate attendanceDate, String tenantId) {
        log.debug("Validating teacher attendance request for teacher: {}", request.getTeacherId());
        
        // Validate required fields
        if (request.getTeacherId() == null) {
            log.error("Validation failed: Teacher ID is required");
            throw new BusinessException("Teacher ID is required");
        }
        
        // Check for placeholder/invalid UUID
        UUID placeholderUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (request.getTeacherId().equals(placeholderUUID)) {
            log.error("Validation failed: Invalid UUID format - Teacher ID must be a valid 36-character UUID");
            throw new BusinessException("Invalid UUID format: Teacher ID must be a valid 36-character UUID (e.g., 550e8400-e29b-41d4-a716-446655440000). Please replace placeholder values with actual teacher UUIDs.");
        }
        
        if (request.getStatus() == null) {
            log.error("Validation failed: Attendance status is required");
            throw new BusinessException("Attendance status is required");
        }
        
        // Validate teacher exists
        Teacher teacher = teacherRepository.findByIdAndTenantId(request.getTeacherId(), tenantId)
                .orElseThrow(() -> {
                    log.error("Validation failed: Teacher not found - ID: {}, Tenant: {}", 
                            request.getTeacherId(), tenantId);
                    return new ResourceNotFoundException(
                            "Teacher not found with ID: " + request.getTeacherId() + ". Please verify the teacher exists in your system.");
                });
        
        // Check for duplicate attendance on the same date
        if (attendanceRepository.existsByTeacherIdAndAttendanceDateAndTenantId(
                request.getTeacherId(), attendanceDate, tenantId)) {
            log.warn("Validation failed: Attendance already marked for teacher {} on date {}", 
                    request.getTeacherId(), attendanceDate);
            throw new BusinessException("Attendance already marked for this teacher on date: " + attendanceDate);
        }
        
        log.debug("Teacher attendance validation passed for teacher: {}", request.getTeacherId());
    }

    /**
     * Group teachers by department for efficient processing
     */
    private Map<String, List<ValidatedTeacherAttendanceRequest>> groupTeachersByDepartment(
            List<ValidatedTeacherAttendanceRequest> validatedRequests) {
        
        Map<String, List<ValidatedTeacherAttendanceRequest>> groupedTeachers = new HashMap<>();
        
        for (ValidatedTeacherAttendanceRequest validatedRequest : validatedRequests) {
            String departmentKey = getTeacherDepartment(validatedRequest.getOriginalRequest().getTeacherId());
            groupedTeachers.computeIfAbsent(departmentKey, k -> new ArrayList<>()).add(validatedRequest);
        }
        
        return groupedTeachers;
    }

    /**
     * Get teacher department
     */
    private String getTeacherDepartment(UUID teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        return teacher != null && teacher.getDepartment() != null ? 
                teacher.getDepartment() : "UNKNOWN_DEPARTMENT";
    }

    /**
     * Process teachers for a specific department
     */
    private void processDepartmentTeachers(
            List<ValidatedTeacherAttendanceRequest> departmentTeachers, String departmentKey, String tenantId,
            List<AttendanceResponse> createdAttendanceRecords, List<BulkTeacherAttendanceResponse.BulkError> errors,
            Map<String, BulkTeacherAttendanceResponse.DepartmentSummary> departmentSummaries, LocalDate attendanceDate) {
        
        int successfulInDepartment = 0;
        int failedInDepartment = 0;
        
        log.info("Processing {} teachers for department: {}", departmentTeachers.size(), departmentKey);
        
        for (ValidatedTeacherAttendanceRequest validatedTeacher : departmentTeachers) {
            String teacherIdentifier = getTeacherIdentifier(validatedTeacher.getOriginalRequest());
            int rowIndex = validatedTeacher.getOriginalIndex();
            
            try {
                log.debug("Marking attendance for teacher: {} (row: {}) in department: {}", 
                        teacherIdentifier, rowIndex, departmentKey);
                
                // Create attendance using existing single attendance marking logic
                AttendanceResponse attendanceResponse = markSingleTeacherAttendance(
                        validatedTeacher.getOriginalRequest(), tenantId, attendanceDate);
                createdAttendanceRecords.add(attendanceResponse);
                successfulInDepartment++;
                
                log.info("Successfully marked attendance for teacher: {} (ID: {}) in department: {}", 
                        teacherIdentifier, attendanceResponse.getId(), departmentKey);
                        
            } catch (BusinessException e) {
                failedInDepartment++;
                String errorType = determineErrorType(e.getMessage());
                
                errors.add(BulkTeacherAttendanceResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .teacherId(validatedTeacher.getOriginalRequest().getTeacherId())
                        .errorMessage(e.getMessage())
                        .errorType(errorType)
                        .failedRequest(validatedTeacher.getOriginalRequest())
                        .teacherIdentifier(teacherIdentifier)
                        .build());
                
                log.warn("Business error marking attendance for teacher {} (row: {}): {}", 
                        teacherIdentifier, rowIndex, e.getMessage());
                        
            } catch (ResourceNotFoundException e) {
                failedInDepartment++;
                
                errors.add(BulkTeacherAttendanceResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .teacherId(validatedTeacher.getOriginalRequest().getTeacherId())
                        .errorMessage(e.getMessage())
                        .errorType("RESOURCE_NOT_FOUND")
                        .failedRequest(validatedTeacher.getOriginalRequest())
                        .teacherIdentifier(teacherIdentifier)
                        .build());
                
                log.error("Resource not found error marking attendance for teacher {} (row: {}): {}", 
                        teacherIdentifier, rowIndex, e.getMessage());
                        
            } catch (Exception e) {
                failedInDepartment++;
                
                errors.add(BulkTeacherAttendanceResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .teacherId(validatedTeacher.getOriginalRequest().getTeacherId())
                        .errorMessage("Unexpected error: " + e.getMessage())
                        .errorType("SYSTEM_ERROR")
                        .failedRequest(validatedTeacher.getOriginalRequest())
                        .teacherIdentifier(teacherIdentifier)
                        .build());
                
                log.error("Unexpected error marking attendance for teacher {} (row: {}): {}", 
                        teacherIdentifier, rowIndex, e.getMessage(), e);
            }
        }
        
        // Add department summary
        departmentSummaries.put(departmentKey, BulkTeacherAttendanceResponse.DepartmentSummary.builder()
                .departmentName(departmentKey)
                .totalRequested(departmentTeachers.size())
                .successful(successfulInDepartment)
                .failed(failedInDepartment)
                .build());
        
        log.info("Department processing completed for {}: {} successful, {} failed", 
                departmentKey, successfulInDepartment, failedInDepartment);
    }

    /**
     * Mark attendance for a single teacher
     */
    private AttendanceResponse markSingleTeacherAttendance(
            BulkTeacherAttendanceRequest.TeacherAttendanceRecord request, String tenantId, LocalDate attendanceDate) {
        
        // Get teacher details
        Teacher teacher = teacherRepository.findByIdAndTenantId(request.getTeacherId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getTeacherId()));
        
        // Create attendance record for teacher
        // Note: Using studentId field to store teacherId for teacher attendance
        // Using teacherClassId as null to distinguish from student attendance
        Attendance attendance = Attendance.builder()
                .studentId(request.getTeacherId()) // Store teacherId in studentId field
                .teacherClassId(null) // Null for teacher attendance
                .attendanceDate(attendanceDate) // Use the date from request
                .status(request.getStatus())
                .markedAt(LocalTime.now())
                .remarks(request.getRemarks())
                .build();
        
        // Set tenant and other required fields
        attendance.setTenantId(tenantId);
        
        // Save attendance
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Convert to response
        return AttendanceResponse.builder()
                .id(savedAttendance.getId().toString())
                .teacherId(request.getTeacherId())
                .teacherName(getTeacherIdentifier(request))
                .employeeId(teacher.getEmployeeId())
                .department(teacher.getDepartment())
                .attendanceDate(savedAttendance.getAttendanceDate())
                .status(savedAttendance.getStatus().toString())
                .markedAt(savedAttendance.getMarkedAt())
                .remarks(savedAttendance.getRemarks())
                .markedByTeacherId("SYSTEM") // Simplified - you might want to get actual user
                .markedByTeacherName("System") // Simplified - you might want to get actual name
                .build();
    }

    /**
     * Determine error type based on error message
     */
    private String determineErrorType(String errorMessage) {
        if (errorMessage == null) {
            return "UNKNOWN_ERROR";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("already marked") || lowerMessage.contains("already exists")) {
            return "DUPLICATE_ATTENDANCE";
        } else if (lowerMessage.contains("teacher not found")) {
            return "TEACHER_NOT_FOUND";
        } else if (lowerMessage.contains("required")) {
            return "MISSING_REQUIRED_FIELD";
        } else if (lowerMessage.contains("invalid") && lowerMessage.contains("format")) {
            return "INVALID_FORMAT";
        } else {
            return "BUSINESS_ERROR";
        }
    }

    /**
     * Get teacher identifier for error reporting
     */
    private String getTeacherIdentifier(BulkTeacherAttendanceRequest.TeacherAttendanceRecord request) {
        Teacher teacher = teacherRepository.findById(request.getTeacherId()).orElse(null);
        if (teacher != null) {
            String name = "";
            if (teacher.getFirstName() != null) {
                name += teacher.getFirstName();
            }
            if (teacher.getMiddleName() != null) {
                name += " " + teacher.getMiddleName();
            }
            if (teacher.getLastName() != null) {
                name += " " + teacher.getLastName();
            }
            return name.trim();
        }
        return "Unknown Teacher";
    }

    /**
     * Build error response
     */
    private BulkTeacherAttendanceResponse buildErrorResponse(String batchReference, int totalRequested, 
            List<BulkTeacherAttendanceResponse.BulkError> errors) {
        return BulkTeacherAttendanceResponse.builder()
                .batchReference(batchReference)
                .totalRequested(totalRequested)
                .successful(0)
                .failed(errors.size())
                .createdAttendanceRecords(new ArrayList<>())
                .errors(errors)
                .departmentSummaries(new HashMap<>())
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get teacher attendance records with filtering
     */
    public List<AttendanceResponse> getTeacherAttendanceRecords(LocalDate date, UUID teacherId, String department) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting teacher attendance records for tenant: {}, date: {}, teacherId: {}, department: {}", 
                tenantId, date, teacherId, department);
        
        // For now, return empty list - you'll need to implement actual query logic
        // This would typically query the Attendance table with appropriate filters
        return new ArrayList<>();
    }

    /**
     * Update single teacher attendance record
     */
    public AttendanceResponse updateTeacherAttendance(UUID id, BulkTeacherAttendanceRequest.TeacherAttendanceRecord request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating teacher attendance record: {} for tenant: {}", id, tenantId);
        
        // Find existing attendance record
        Attendance existingAttendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + id));
        
        // Validate ownership
        if (!existingAttendance.getTenantId().equals(tenantId)) {
            throw new BusinessException("Access denied: Attendance record does not belong to your tenant");
        }
        
        // Update fields
        if (request.getStatus() != null) {
            existingAttendance.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            existingAttendance.setRemarks(request.getRemarks());
        }
        if (request.getCheckInTime() != null) {
            // You might want to parse and store check-in time
            existingAttendance.setRemarks(existingAttendance.getRemarks() + " | Check-in: " + request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            // You might want to parse and store check-out time
            existingAttendance.setRemarks(existingAttendance.getRemarks() + " | Check-out: " + request.getCheckOutTime());
        }
        
        // Save updated record
        Attendance updatedAttendance = attendanceRepository.save(existingAttendance);
        
        log.info("Successfully updated teacher attendance record: {}", id);
        
        // Convert to response
        return AttendanceResponse.builder()
                .id(updatedAttendance.getId().toString())
                .teacherId(existingAttendance.getStudentId()) // Teacher ID stored in studentId field
                .attendanceDate(updatedAttendance.getAttendanceDate())
                .status(updatedAttendance.getStatus().toString())
                .markedAt(updatedAttendance.getMarkedAt())
                .remarks(updatedAttendance.getRemarks())
                .teacherName("Updated Teacher") // You might want to get actual name
                .build();
    }

    /**
     * Delete teacher attendance record
     */
    public void deleteTeacherAttendance(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting teacher attendance record: {} for tenant: {}", id, tenantId);
        
        // Find existing attendance record
        Attendance existingAttendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + id));
        
        // Validate ownership
        if (!existingAttendance.getTenantId().equals(tenantId)) {
            throw new BusinessException("Access denied: Attendance record does not belong to your tenant");
        }
        
        // Delete the record
        attendanceRepository.delete(existingAttendance);
        
        log.info("Successfully deleted teacher attendance record: {}", id);
    }

    /**
     * Inner class to hold validated request with original index
     */
    private static class ValidatedTeacherAttendanceRequest {
        private BulkTeacherAttendanceRequest.TeacherAttendanceRecord originalRequest;
        private int originalIndex;
        
        public ValidatedTeacherAttendanceRequest() {}
        
        public ValidatedTeacherAttendanceRequest(BulkTeacherAttendanceRequest.TeacherAttendanceRecord originalRequest, int originalIndex) {
            this.originalRequest = originalRequest;
            this.originalIndex = originalIndex;
        }
        
        public BulkTeacherAttendanceRequest.TeacherAttendanceRecord getOriginalRequest() {
            return originalRequest;
        }
        
        public void setOriginalRequest(BulkTeacherAttendanceRequest.TeacherAttendanceRecord originalRequest) {
            this.originalRequest = originalRequest;
        }
        
        public int getOriginalIndex() {
            return originalIndex;
        }
        
        public void setOriginalIndex(int originalIndex) {
            this.originalIndex = originalIndex;
        }
        
        public static ValidatedTeacherAttendanceRequestBuilder builder() {
            return new ValidatedTeacherAttendanceRequestBuilder();
        }
        
        public static class ValidatedTeacherAttendanceRequestBuilder {
            private BulkTeacherAttendanceRequest.TeacherAttendanceRecord originalRequest;
            private int originalIndex;
            
            public ValidatedTeacherAttendanceRequestBuilder originalRequest(BulkTeacherAttendanceRequest.TeacherAttendanceRecord originalRequest) {
                this.originalRequest = originalRequest;
                return this;
            }
            
            public ValidatedTeacherAttendanceRequestBuilder originalIndex(int originalIndex) {
                this.originalIndex = originalIndex;
                return this;
            }
            
            public ValidatedTeacherAttendanceRequest build() {
                return new ValidatedTeacherAttendanceRequest(originalRequest, originalIndex);
            }
        }
    }
}
