package com.schoolmgmt.service;

import com.schoolmgmt.model.LeaveBalance;
import com.schoolmgmt.model.LeaveRequest;
import com.schoolmgmt.model.LeaveType;
import com.schoolmgmt.repository.LeaveBalanceRepository;
import com.schoolmgmt.repository.LeaveRequestRepository;
import com.schoolmgmt.repository.LeaveTypeRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    // ===== Leave Type Operations =====

    @Transactional
    public LeaveType createLeaveType(LeaveType leaveType) {
        String tenantId = TenantContext.getCurrentTenant();
        leaveType.setTenantId(tenantId);

        if (leaveTypeRepository.existsByTenantIdAndName(tenantId, leaveType.getName())) {
            throw new IllegalArgumentException("Leave type '" + leaveType.getName() + "' already exists");
        }

        log.info("Creating leave type: {} for tenant: {}", leaveType.getName(), tenantId);
        return leaveTypeRepository.save(leaveType);
    }

    @Transactional(readOnly = true)
    public List<LeaveType> getLeaveTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveTypeRepository.findByTenantIdOrderByName(tenantId);
    }

    @Transactional(readOnly = true)
    public List<LeaveType> getActiveLeaveTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveTypeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public LeaveType updateLeaveType(UUID id, LeaveType updated) {
        LeaveType existing = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave type not found: " + id));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getMaxDaysPerYear() != null) existing.setMaxDaysPerYear(updated.getMaxDaysPerYear());
        if (updated.getIsPaid() != null) existing.setIsPaid(updated.getIsPaid());
        if (updated.getRequiresApproval() != null) existing.setRequiresApproval(updated.getRequiresApproval());
        if (updated.getApplicableRoles() != null) existing.setApplicableRoles(updated.getApplicableRoles());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return leaveTypeRepository.save(existing);
    }

    @Transactional
    public void deleteLeaveType(UUID id) {
        leaveTypeRepository.deleteById(id);
    }

    // ===== Leave Request Operations =====

    @Transactional
    public LeaveRequest applyLeave(LeaveRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        request.setTenantId(tenantId);
        request.setStatus(LeaveRequest.LeaveStatus.PENDING);

        // Check for overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                tenantId, request.getUserId(), request.getStartDate(), request.getEndDate());

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Leave dates overlap with an existing approved leave");
        }

        log.info("Leave applied by {} from {} to {}", request.getUserId(),
                request.getStartDate(), request.getEndDate());
        return leaveRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getMyLeaves(String userId) {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveRequestRepository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingApprovals() {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveRequestRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(
                tenantId, LeaveRequest.LeaveStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequest> getAllLeaveRequests(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    @Transactional
    public LeaveRequest approveLeave(UUID id, String approvedBy, String approvedByName, String remarks) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave request not found: " + id));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is not in pending state");
        }

        request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedByName(approvedByName);
        request.setApprovalRemarks(remarks);
        request.setApprovedAt(LocalDateTime.now());

        // Update leave balance
        updateLeaveBalance(request, true);

        log.info("Leave {} approved by {}", id, approvedBy);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeave(UUID id, String rejectedBy, String rejectedByName, String remarks) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave request not found: " + id));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is not in pending state");
        }

        request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setApprovedBy(rejectedBy);
        request.setApprovedByName(rejectedByName);
        request.setApprovalRemarks(remarks);
        request.setApprovedAt(LocalDateTime.now());

        log.info("Leave {} rejected by {}", id, rejectedBy);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest cancelLeave(UUID id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave request not found: " + id));

        if (request.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
            // Restore balance
            updateLeaveBalance(request, false);
        }

        request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        log.info("Leave {} cancelled", id);
        return leaveRequestRepository.save(request);
    }

    // ===== Leave Balance Operations =====

    @Transactional(readOnly = true)
    public List<LeaveBalance> getLeaveBalance(String userId, String academicYear) {
        String tenantId = TenantContext.getCurrentTenant();
        return leaveBalanceRepository.findByTenantIdAndUserIdAndAcademicYear(
                tenantId, userId, academicYear);
    }

    @Transactional
    public LeaveBalance initializeBalance(String userId, UUID leaveTypeId,
                                           String academicYear, int totalDays) {
        String tenantId = TenantContext.getCurrentTenant();

        Optional<LeaveBalance> existing = leaveBalanceRepository
                .findByTenantIdAndUserIdAndLeaveTypeIdAndAcademicYear(
                        tenantId, userId, leaveTypeId, academicYear);

        if (existing.isPresent()) {
            LeaveBalance balance = existing.get();
            balance.setTotalAllocated(totalDays);
            return leaveBalanceRepository.save(balance);
        }

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new NoSuchElementException("Leave type not found: " + leaveTypeId));

        LeaveBalance balance = LeaveBalance.builder()
                .tenantId(tenantId)
                .userId(userId)
                .leaveType(leaveType)
                .academicYear(academicYear)
                .totalAllocated(totalDays)
                .used(0)
                .pending(0)
                .build();

        return leaveBalanceRepository.save(balance);
    }

    // ===== Summary =====

    @Transactional(readOnly = true)
    public Map<String, Object> getLeaveSummary(String userId) {
        String tenantId = TenantContext.getCurrentTenant();

        String academicYear = getCurrentAcademicYear();
        List<LeaveBalance> balances = leaveBalanceRepository
                .findByTenantIdAndUserIdAndAcademicYear(tenantId, userId, academicYear);

        List<LeaveRequest> pendingRequests = leaveRequestRepository
                .findByTenantIdAndUserIdAndStatusIn(tenantId, userId,
                        List.of(LeaveRequest.LeaveStatus.PENDING));

        Map<String, Object> summary = new HashMap<>();
        summary.put("balances", balances);
        summary.put("pendingRequests", pendingRequests.size());
        summary.put("academicYear", academicYear);

        int totalAllocated = balances.stream().mapToInt(LeaveBalance::getTotalAllocated).sum();
        int totalUsed = balances.stream().mapToInt(LeaveBalance::getUsed).sum();
        int totalPending = balances.stream().mapToInt(LeaveBalance::getPending).sum();

        summary.put("totalAllocated", totalAllocated);
        summary.put("totalUsed", totalUsed);
        summary.put("totalPending", totalPending);
        summary.put("totalRemaining", totalAllocated - totalUsed - totalPending);

        return summary;
    }

    // ===== Private Helpers =====

    private void updateLeaveBalance(LeaveRequest request, boolean isApproval) {
        String tenantId = request.getTenantId();
        String academicYear = getCurrentAcademicYear();

        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository
                .findByTenantIdAndUserIdAndLeaveTypeIdAndAcademicYear(
                        tenantId, request.getUserId(),
                        request.getLeaveType().getId(), academicYear);

        if (balanceOpt.isPresent()) {
            LeaveBalance balance = balanceOpt.get();
            if (isApproval) {
                balance.setUsed(balance.getUsed() + request.getTotalDays());
            } else {
                // Cancellation: restore days
                balance.setUsed(Math.max(0, balance.getUsed() - request.getTotalDays()));
            }
            leaveBalanceRepository.save(balance);
        }
    }

    private String getCurrentAcademicYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (month >= 4) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}
