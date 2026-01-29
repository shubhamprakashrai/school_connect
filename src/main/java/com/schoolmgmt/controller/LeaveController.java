package com.schoolmgmt.controller;

import com.schoolmgmt.model.LeaveBalance;
import com.schoolmgmt.model.LeaveRequest;
import com.schoolmgmt.model.LeaveType;
import com.schoolmgmt.model.User;
import com.schoolmgmt.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leave Management", description = "APIs for managing leave types, requests, and balances")
public class LeaveController {

    private final LeaveService leaveService;

    // ===== Leave Type Endpoints =====

    @PostMapping("/types")
    @Operation(summary = "Create leave type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LeaveType> createLeaveType(@Valid @RequestBody LeaveType leaveType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.createLeaveType(leaveType));
    }

    @GetMapping("/types")
    @Operation(summary = "Get leave types")
    public ResponseEntity<List<LeaveType>> getLeaveTypes() {
        return ResponseEntity.ok(leaveService.getLeaveTypes());
    }

    @GetMapping("/types/active")
    @Operation(summary = "Get active leave types")
    public ResponseEntity<List<LeaveType>> getActiveLeaveTypes() {
        return ResponseEntity.ok(leaveService.getActiveLeaveTypes());
    }

    @PutMapping("/types/{id}")
    @Operation(summary = "Update leave type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LeaveType> updateLeaveType(
            @PathVariable UUID id, @Valid @RequestBody LeaveType leaveType) {
        return ResponseEntity.ok(leaveService.updateLeaveType(id, leaveType));
    }

    @DeleteMapping("/types/{id}")
    @Operation(summary = "Delete leave type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable UUID id) {
        leaveService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Leave Request Endpoints =====

    @PostMapping("/request")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<LeaveRequest> applyLeave(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LeaveRequest request) {
        request.setUserId(user.getUserId());
        request.setUserName(user.getFirstName() + " " + user.getLastName());
        log.info("Leave application by user: {}", user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my leaves")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getMyLeaves(user.getUserId()));
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending approvals")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<LeaveRequest>> getPendingApprovals() {
        return ResponseEntity.ok(leaveService.getPendingApprovals());
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all leave requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<LeaveRequest>> getAllLeaveRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaveRequests(pageable));
    }

    @PutMapping("/requests/{id}/approve")
    @Operation(summary = "Approve leave request")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = body != null ? body.get("remarks") : null;
        return ResponseEntity.ok(leaveService.approveLeave(
                id, user.getUserId(),
                user.getFirstName() + " " + user.getLastName(), remarks));
    }

    @PutMapping("/requests/{id}/reject")
    @Operation(summary = "Reject leave request")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = body != null ? body.get("remarks") : null;
        return ResponseEntity.ok(leaveService.rejectLeave(
                id, user.getUserId(),
                user.getFirstName() + " " + user.getLastName(), remarks));
    }

    @PutMapping("/requests/{id}/cancel")
    @Operation(summary = "Cancel leave request")
    public ResponseEntity<LeaveRequest> cancelLeave(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveService.cancelLeave(id));
    }

    // ===== Leave Balance Endpoints =====

    @GetMapping("/balance")
    @Operation(summary = "Get my leave balance")
    public ResponseEntity<List<LeaveBalance>> getMyBalance(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String academicYear) {
        String year = academicYear != null ? academicYear : getCurrentAcademicYear();
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.getUserId(), year));
    }

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Get user's leave balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<LeaveBalance>> getUserBalance(
            @PathVariable String userId,
            @RequestParam(required = false) String academicYear) {
        String year = academicYear != null ? academicYear : getCurrentAcademicYear();
        return ResponseEntity.ok(leaveService.getLeaveBalance(userId, year));
    }

    @PostMapping("/balance/initialize")
    @Operation(summary = "Initialize leave balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LeaveBalance> initializeBalance(
            @RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        UUID leaveTypeId = UUID.fromString((String) body.get("leaveTypeId"));
        String academicYear = (String) body.get("academicYear");
        int totalDays = (Integer) body.get("totalDays");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.initializeBalance(userId, leaveTypeId, academicYear, totalDays));
    }

    // ===== Summary =====

    @GetMapping("/summary")
    @Operation(summary = "Get my leave summary")
    public ResponseEntity<Map<String, Object>> getMySummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaveService.getLeaveSummary(user.getUserId()));
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get user's leave summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserSummary(@PathVariable String userId) {
        return ResponseEntity.ok(leaveService.getLeaveSummary(userId));
    }

    private String getCurrentAcademicYear() {
        int year = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();
        if (month >= 4) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}
