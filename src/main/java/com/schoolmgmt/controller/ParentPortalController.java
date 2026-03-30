package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.model.Parent;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.service.AttendanceService;
import com.schoolmgmt.util.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for parent portal — allows parents to view
 * attendance, grades, fees, and assignments for their linked children.
 */
@RestController
@RequestMapping("/parents/portal")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Parent Portal", description = "Parent portal APIs for viewing child information")
public class ParentPortalController {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final ExamResultRepository examResultRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final AssignmentRepository assignmentRepository;

    // ------------------------------------------------------------------ //
    //  1. List linked children
    // ------------------------------------------------------------------ //

    @GetMapping("/children")
    @Operation(summary = "List linked children",
               description = "Returns all students linked to the authenticated parent")
    public ResponseEntity<ApiResponse> getChildren(@AuthenticationPrincipal User user) {
        Parent parent = resolveParent(user);

        Set<Student> students = parent.getAllStudents();

        List<Map<String, Object>> childrenInfo = students.stream()
                .map(this::toBasicStudentInfo)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Children retrieved successfully", childrenInfo));
    }

    // ------------------------------------------------------------------ //
    //  2. Child attendance
    // ------------------------------------------------------------------ //

    @GetMapping("/children/{studentId}/attendance")
    @Operation(summary = "Get child attendance",
               description = "Returns attendance records and percentage for a linked child")
    public ResponseEntity<ApiResponse> getChildAttendance(
            @AuthenticationPrincipal User user,
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Parent parent = resolveParent(user);
        validateAccess(parent, studentId);

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        String tenantId = TenantContext.getCurrentTenant();
        var records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetweenAndTenantId(studentId, startDate, endDate, tenantId);

        Map<String, Object> percentage = attendanceService.getAttendancePercentage(studentId, startDate, endDate);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("statistics", percentage);

        return ResponseEntity.ok(
                ApiResponse.success("Attendance retrieved successfully", result));
    }

    // ------------------------------------------------------------------ //
    //  3. Child grades / exam results
    // ------------------------------------------------------------------ //

    @GetMapping("/children/{studentId}/grades")
    @Operation(summary = "Get child grades",
               description = "Returns exam results for a linked child")
    public ResponseEntity<ApiResponse> getChildGrades(
            @AuthenticationPrincipal User user,
            @PathVariable UUID studentId) {

        Parent parent = resolveParent(user);
        validateAccess(parent, studentId);

        String tenantId = TenantContext.getCurrentTenant();
        var results = examResultRepository.findByTenantIdAndStudentId(tenantId, studentId);

        return ResponseEntity.ok(
                ApiResponse.success("Grades retrieved successfully", results));
    }

    // ------------------------------------------------------------------ //
    //  4. Child fees
    // ------------------------------------------------------------------ //

    @GetMapping("/children/{studentId}/fees")
    @Operation(summary = "Get child fee status",
               description = "Returns fee payments and balances for a linked child")
    public ResponseEntity<ApiResponse> getChildFees(
            @AuthenticationPrincipal User user,
            @PathVariable UUID studentId) {

        Parent parent = resolveParent(user);
        validateAccess(parent, studentId);

        String tenantId = TenantContext.getCurrentTenant();
        var payments = feePaymentRepository.findByTenantIdAndStudentId(tenantId, studentId);

        return ResponseEntity.ok(
                ApiResponse.success("Fee information retrieved successfully", payments));
    }

    // ------------------------------------------------------------------ //
    //  5. Child assignments
    // ------------------------------------------------------------------ //

    @GetMapping("/children/{studentId}/assignments")
    @Operation(summary = "Get child assignments",
               description = "Returns assignments for the class of a linked child")
    public ResponseEntity<ApiResponse> getChildAssignments(
            @AuthenticationPrincipal User user,
            @PathVariable UUID studentId) {

        Parent parent = resolveParent(user);
        validateAccess(parent, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        String tenantId = TenantContext.getCurrentTenant();
        String classIdStr = student.getCurrentClassId();

        if (classIdStr == null || classIdStr.isBlank()) {
            return ResponseEntity.ok(
                    ApiResponse.success("No class assigned to this student", Collections.emptyList()));
        }

        UUID classId = UUID.fromString(classIdStr);
        var assignments = assignmentRepository.findByClassIdAndTenantId(classId, tenantId);

        return ResponseEntity.ok(
                ApiResponse.success("Assignments retrieved successfully", assignments));
    }

    // ------------------------------------------------------------------ //
    //  Helper methods
    // ------------------------------------------------------------------ //

    private Parent resolveParent(User user) {
        String tenantId = TenantContext.getCurrentTenant();
        return parentRepository.findByUserIdAndTenantId(user.getId(), tenantId)
                .orElseThrow(() -> new IllegalStateException(
                        "No parent profile found for user: " + user.getId()));
    }

    private void validateAccess(Parent parent, UUID studentId) {
        if (!parent.hasAccessToStudent(studentId.toString())) {
            throw new SecurityException(
                    "Parent does not have access to student: " + studentId);
        }
    }

    private Map<String, Object> toBasicStudentInfo(Student student) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", student.getId());
        info.put("firstName", student.getFirstName());
        info.put("lastName", student.getLastName());
        info.put("currentClassId", student.getCurrentClassId());
        info.put("rollNumber", student.getRollNumber());
        info.put("photoUrl", student.getPhotoUrl());
        return info;
    }
}
