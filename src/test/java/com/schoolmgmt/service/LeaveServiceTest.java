package com.schoolmgmt.service;

import com.schoolmgmt.model.LeaveBalance;
import com.schoolmgmt.model.LeaveRequest;
import com.schoolmgmt.model.LeaveType;
import com.schoolmgmt.repository.LeaveBalanceRepository;
import com.schoolmgmt.repository.LeaveRequestRepository;
import com.schoolmgmt.repository.LeaveTypeRepository;
import com.schoolmgmt.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @InjectMocks
    private LeaveService leaveService;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String USER_ID = "user-001";
    private static final String APPROVER_ID = "admin-001";
    private static final String APPROVER_NAME = "Admin User";

    private LeaveType sickLeaveType;
    private LeaveRequest sampleLeaveRequest;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

        sickLeaveType = LeaveType.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .name("SICK")
                .description("Sick Leave")
                .maxDaysPerYear(12)
                .isPaid(true)
                .requiresApproval(true)
                .isActive(true)
                .build();

        sampleLeaveRequest = LeaveRequest.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .leaveType(sickLeaveType)
                .userId(USER_ID)
                .userName("Test User")
                .userRole("TEACHER")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalDays(3)
                .reason("Medical appointment")
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // =========================================================================
    // applyLeave tests
    // =========================================================================

    @Nested
    @DisplayName("applyLeave")
    class ApplyLeaveTests {

        @Test
        @DisplayName("should successfully apply leave with valid request and no overlapping dates")
        void applyLeave_withValidRequest_shouldSaveAndReturnLeaveRequest() {
            // Arrange
            LeaveRequest inputRequest = LeaveRequest.builder()
                    .leaveType(sickLeaveType)
                    .userId(USER_ID)
                    .userName("Test User")
                    .userRole("TEACHER")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(7))
                    .totalDays(3)
                    .reason("Flu recovery")
                    .build();

            when(leaveRequestRepository.findOverlappingLeaves(
                    eq(TENANT_ID), eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            LeaveRequest savedRequest = LeaveRequest.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .leaveType(sickLeaveType)
                    .userId(USER_ID)
                    .userName("Test User")
                    .userRole("TEACHER")
                    .startDate(inputRequest.getStartDate())
                    .endDate(inputRequest.getEndDate())
                    .totalDays(3)
                    .reason("Flu recovery")
                    .status(LeaveRequest.LeaveStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

            // Act
            LeaveRequest result = leaveService.applyLeave(inputRequest);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(TENANT_ID, result.getTenantId());
            assertEquals(LeaveRequest.LeaveStatus.PENDING, result.getStatus());
            assertEquals(USER_ID, result.getUserId());
            assertEquals("Flu recovery", result.getReason());

            verify(leaveRequestRepository).findOverlappingLeaves(
                    eq(TENANT_ID), eq(USER_ID),
                    eq(inputRequest.getStartDate()), eq(inputRequest.getEndDate()));
            verify(leaveRequestRepository).save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("should set status to PENDING and assign tenant ID on apply")
        void applyLeave_shouldSetPendingStatusAndTenantId() {
            // Arrange
            LeaveRequest inputRequest = LeaveRequest.builder()
                    .leaveType(sickLeaveType)
                    .userId(USER_ID)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .reason("Personal")
                    .build();

            when(leaveRequestRepository.findOverlappingLeaves(
                    any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenAnswer(invocation -> {
                        LeaveRequest req = invocation.getArgument(0);
                        req.setId(UUID.randomUUID());
                        return req;
                    });

            // Act
            LeaveRequest result = leaveService.applyLeave(inputRequest);

            // Assert
            assertEquals(TENANT_ID, result.getTenantId());
            assertEquals(LeaveRequest.LeaveStatus.PENDING, result.getStatus());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when leave dates overlap with existing approved leave")
        void applyLeave_withOverlappingDates_shouldThrowException() {
            // Arrange
            LeaveRequest overlappingRequest = LeaveRequest.builder()
                    .leaveType(sickLeaveType)
                    .userId(USER_ID)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .reason("Overlap test")
                    .build();

            LeaveRequest existingApproved = LeaveRequest.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .leaveType(sickLeaveType)
                    .userId(USER_ID)
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(4))
                    .status(LeaveRequest.LeaveStatus.APPROVED)
                    .build();

            when(leaveRequestRepository.findOverlappingLeaves(
                    eq(TENANT_ID), eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(existingApproved));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> leaveService.applyLeave(overlappingRequest));

            assertTrue(exception.getMessage().contains("overlap"));
            verify(leaveRequestRepository, never()).save(any());
        }
    }

    // =========================================================================
    // approveLeave tests
    // =========================================================================

    @Nested
    @DisplayName("approveLeave")
    class ApproveLeaveTests {

        @Test
        @DisplayName("should approve pending leave, update status, set approver info, and update balance")
        void approveLeave_withPendingRequest_shouldUpdateStatusAndBalance() {
            // Arrange
            UUID leaveId = sampleLeaveRequest.getId();
            String remarks = "Approved for medical reasons";

            when(leaveRequestRepository.findById(leaveId))
                    .thenReturn(Optional.of(sampleLeaveRequest));

            // Set up leave balance to verify balance update
            LeaveBalance balance = LeaveBalance.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .userId(USER_ID)
                    .leaveType(sickLeaveType)
                    .academicYear(getCurrentAcademicYear())
                    .totalAllocated(12)
                    .used(2)
                    .pending(0)
                    .build();

            when(leaveBalanceRepository.findByTenantIdAndUserIdAndLeaveTypeIdAndAcademicYear(
                    eq(TENANT_ID), eq(USER_ID), eq(sickLeaveType.getId()), anyString()))
                    .thenReturn(Optional.of(balance));

            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            LeaveRequest result = leaveService.approveLeave(leaveId, APPROVER_ID, APPROVER_NAME, remarks);

            // Assert
            assertEquals(LeaveRequest.LeaveStatus.APPROVED, result.getStatus());
            assertEquals(APPROVER_ID, result.getApprovedBy());
            assertEquals(APPROVER_NAME, result.getApprovedByName());
            assertEquals(remarks, result.getApprovalRemarks());
            assertNotNull(result.getApprovedAt());

            // Verify balance was updated: used should go from 2 to 2 + 3 = 5
            verify(leaveBalanceRepository).save(argThat(b ->
                    b.getUsed() == 5));
            verify(leaveRequestRepository).save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("should throw NoSuchElementException when leave request not found")
        void approveLeave_withNonExistentId_shouldThrowException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(leaveRequestRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchElementException.class,
                    () -> leaveService.approveLeave(nonExistentId, APPROVER_ID, APPROVER_NAME, ""));
        }

        @Test
        @DisplayName("should throw IllegalStateException when leave is not in PENDING state")
        void approveLeave_withNonPendingRequest_shouldThrowException() {
            // Arrange
            sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            when(leaveRequestRepository.findById(sampleLeaveRequest.getId()))
                    .thenReturn(Optional.of(sampleLeaveRequest));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> leaveService.approveLeave(
                            sampleLeaveRequest.getId(), APPROVER_ID, APPROVER_NAME, ""));

            assertTrue(exception.getMessage().contains("not in pending state"));
        }
    }

    // =========================================================================
    // rejectLeave tests
    // =========================================================================

    @Nested
    @DisplayName("rejectLeave")
    class RejectLeaveTests {

        @Test
        @DisplayName("should reject pending leave and update status to REJECTED")
        void rejectLeave_withPendingRequest_shouldUpdateStatusToRejected() {
            // Arrange
            UUID leaveId = sampleLeaveRequest.getId();
            String rejectRemarks = "Insufficient staff coverage";

            when(leaveRequestRepository.findById(leaveId))
                    .thenReturn(Optional.of(sampleLeaveRequest));
            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            LeaveRequest result = leaveService.rejectLeave(leaveId, APPROVER_ID, APPROVER_NAME, rejectRemarks);

            // Assert
            assertEquals(LeaveRequest.LeaveStatus.REJECTED, result.getStatus());
            assertEquals(APPROVER_ID, result.getApprovedBy());
            assertEquals(APPROVER_NAME, result.getApprovedByName());
            assertEquals(rejectRemarks, result.getApprovalRemarks());
            assertNotNull(result.getApprovedAt());

            verify(leaveRequestRepository).save(any(LeaveRequest.class));
            // Balance should NOT be updated on rejection
            verify(leaveBalanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalStateException when rejecting non-pending leave")
        void rejectLeave_withNonPendingRequest_shouldThrowException() {
            // Arrange
            sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
            when(leaveRequestRepository.findById(sampleLeaveRequest.getId()))
                    .thenReturn(Optional.of(sampleLeaveRequest));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> leaveService.rejectLeave(
                            sampleLeaveRequest.getId(), APPROVER_ID, APPROVER_NAME, "Denied"));

            assertTrue(exception.getMessage().contains("not in pending state"));
        }

        @Test
        @DisplayName("should throw NoSuchElementException when leave request not found for rejection")
        void rejectLeave_withNonExistentId_shouldThrowException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(leaveRequestRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchElementException.class,
                    () -> leaveService.rejectLeave(nonExistentId, APPROVER_ID, APPROVER_NAME, ""));
        }
    }

    // =========================================================================
    // cancelLeave tests
    // =========================================================================

    @Nested
    @DisplayName("cancelLeave")
    class CancelLeaveTests {

        @Test
        @DisplayName("should cancel a pending leave without restoring balance")
        void cancelLeave_withPendingRequest_shouldSetCancelledWithoutRestoringBalance() {
            // Arrange
            sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
            when(leaveRequestRepository.findById(sampleLeaveRequest.getId()))
                    .thenReturn(Optional.of(sampleLeaveRequest));
            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            LeaveRequest result = leaveService.cancelLeave(sampleLeaveRequest.getId());

            // Assert
            assertEquals(LeaveRequest.LeaveStatus.CANCELLED, result.getStatus());
            verify(leaveRequestRepository).save(any(LeaveRequest.class));
            // Balance should NOT be updated for pending cancellation (only for APPROVED)
            verify(leaveBalanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("should cancel an approved leave and restore the leave balance")
        void cancelLeave_withApprovedRequest_shouldRestoreBalance() {
            // Arrange
            sampleLeaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            sampleLeaveRequest.setTotalDays(3);

            LeaveBalance balance = LeaveBalance.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .userId(USER_ID)
                    .leaveType(sickLeaveType)
                    .academicYear(getCurrentAcademicYear())
                    .totalAllocated(12)
                    .used(5)
                    .pending(0)
                    .build();

            when(leaveRequestRepository.findById(sampleLeaveRequest.getId()))
                    .thenReturn(Optional.of(sampleLeaveRequest));
            when(leaveBalanceRepository.findByTenantIdAndUserIdAndLeaveTypeIdAndAcademicYear(
                    eq(TENANT_ID), eq(USER_ID), eq(sickLeaveType.getId()), anyString()))
                    .thenReturn(Optional.of(balance));
            when(leaveRequestRepository.save(any(LeaveRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            LeaveRequest result = leaveService.cancelLeave(sampleLeaveRequest.getId());

            // Assert
            assertEquals(LeaveRequest.LeaveStatus.CANCELLED, result.getStatus());
            // Balance should be restored: used goes from 5 to 5 - 3 = 2
            verify(leaveBalanceRepository).save(argThat(b -> b.getUsed() == 2));
        }

        @Test
        @DisplayName("should throw NoSuchElementException when cancelling non-existent leave")
        void cancelLeave_withNonExistentId_shouldThrowException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(leaveRequestRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchElementException.class,
                    () -> leaveService.cancelLeave(nonExistentId));
        }
    }

    // =========================================================================
    // getLeaveBalance tests
    // =========================================================================

    @Nested
    @DisplayName("getLeaveBalance")
    class GetLeaveBalanceTests {

        @Test
        @DisplayName("should return correct leave balances for a user in a given academic year")
        void getLeaveBalance_shouldReturnCorrectBalances() {
            // Arrange
            String academicYear = getCurrentAcademicYear();

            LeaveType casualLeaveType = LeaveType.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .name("CASUAL")
                    .maxDaysPerYear(10)
                    .isPaid(true)
                    .build();

            LeaveBalance sickBalance = LeaveBalance.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .userId(USER_ID)
                    .leaveType(sickLeaveType)
                    .academicYear(academicYear)
                    .totalAllocated(12)
                    .used(3)
                    .pending(1)
                    .build();

            LeaveBalance casualBalance = LeaveBalance.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .userId(USER_ID)
                    .leaveType(casualLeaveType)
                    .academicYear(academicYear)
                    .totalAllocated(10)
                    .used(2)
                    .pending(0)
                    .build();

            when(leaveBalanceRepository.findByTenantIdAndUserIdAndAcademicYear(
                    TENANT_ID, USER_ID, academicYear))
                    .thenReturn(List.of(sickBalance, casualBalance));

            // Act
            List<LeaveBalance> balances = leaveService.getLeaveBalance(USER_ID, academicYear);

            // Assert
            assertNotNull(balances);
            assertEquals(2, balances.size());

            LeaveBalance returnedSick = balances.stream()
                    .filter(b -> b.getLeaveType().getName().equals("SICK"))
                    .findFirst()
                    .orElseThrow();

            assertEquals(12, returnedSick.getTotalAllocated());
            assertEquals(3, returnedSick.getUsed());
            assertEquals(1, returnedSick.getPending());
            assertEquals(8, returnedSick.getRemaining()); // 12 - 3 - 1

            LeaveBalance returnedCasual = balances.stream()
                    .filter(b -> b.getLeaveType().getName().equals("CASUAL"))
                    .findFirst()
                    .orElseThrow();

            assertEquals(10, returnedCasual.getTotalAllocated());
            assertEquals(2, returnedCasual.getUsed());
            assertEquals(0, returnedCasual.getPending());
            assertEquals(8, returnedCasual.getRemaining()); // 10 - 2 - 0
        }

        @Test
        @DisplayName("should return empty list when no balances exist for the user")
        void getLeaveBalance_withNoBalances_shouldReturnEmptyList() {
            // Arrange
            String academicYear = getCurrentAcademicYear();

            when(leaveBalanceRepository.findByTenantIdAndUserIdAndAcademicYear(
                    TENANT_ID, USER_ID, academicYear))
                    .thenReturn(Collections.emptyList());

            // Act
            List<LeaveBalance> balances = leaveService.getLeaveBalance(USER_ID, academicYear);

            // Assert
            assertNotNull(balances);
            assertTrue(balances.isEmpty());
        }

        @Test
        @DisplayName("should query with correct tenant ID from context")
        void getLeaveBalance_shouldUseCorrectTenantIdFromContext() {
            // Arrange
            String academicYear = "2025-2026";
            when(leaveBalanceRepository.findByTenantIdAndUserIdAndAcademicYear(
                    TENANT_ID, USER_ID, academicYear))
                    .thenReturn(Collections.emptyList());

            // Act
            leaveService.getLeaveBalance(USER_ID, academicYear);

            // Assert
            verify(leaveBalanceRepository).findByTenantIdAndUserIdAndAcademicYear(
                    TENANT_ID, USER_ID, academicYear);
        }
    }

    // =========================================================================
    // getLeaveSummary tests
    // =========================================================================

    @Nested
    @DisplayName("getLeaveSummary")
    class GetLeaveSummaryTests {

        @Test
        @DisplayName("should return correct totals in summary map")
        void getLeaveSummary_shouldReturnCorrectTotals() {
            // Arrange
            String academicYear = getCurrentAcademicYear();

            LeaveBalance balance1 = LeaveBalance.builder()
                    .totalAllocated(12)
                    .used(3)
                    .pending(1)
                    .build();

            LeaveBalance balance2 = LeaveBalance.builder()
                    .totalAllocated(10)
                    .used(2)
                    .pending(0)
                    .build();

            when(leaveBalanceRepository.findByTenantIdAndUserIdAndAcademicYear(
                    eq(TENANT_ID), eq(USER_ID), eq(academicYear)))
                    .thenReturn(List.of(balance1, balance2));

            when(leaveRequestRepository.findByTenantIdAndUserIdAndStatusIn(
                    eq(TENANT_ID), eq(USER_ID), anyList()))
                    .thenReturn(List.of(sampleLeaveRequest));

            // Act
            Map<String, Object> summary = leaveService.getLeaveSummary(USER_ID);

            // Assert
            assertNotNull(summary);
            assertEquals(22, summary.get("totalAllocated"));    // 12 + 10
            assertEquals(5, summary.get("totalUsed"));           // 3 + 2
            assertEquals(1, summary.get("totalPending"));        // 1 + 0
            assertEquals(16, summary.get("totalRemaining"));     // 22 - 5 - 1
            assertEquals(1, summary.get("pendingRequests"));
            assertEquals(academicYear, summary.get("academicYear"));
        }
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    /**
     * Mirrors the LeaveService.getCurrentAcademicYear() logic for test setup.
     */
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
