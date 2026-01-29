package com.schoolmgmt.service;

import com.schoolmgmt.model.FeePayment;
import com.schoolmgmt.model.FeeStructure;
import com.schoolmgmt.model.FeeType;
import com.schoolmgmt.repository.FeePaymentRepository;
import com.schoolmgmt.repository.FeeStructureRepository;
import com.schoolmgmt.repository.FeeTypeRepository;
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

import java.math.BigDecimal;
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
class FeeServiceTest {

    @InjectMocks
    private FeeService feeService;

    @Mock
    private FeeTypeRepository feeTypeRepository;

    @Mock
    private FeeStructureRepository feeStructureRepository;

    @Mock
    private FeePaymentRepository feePaymentRepository;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final String STUDENT_NAME = "John Doe";

    private FeeType tuitionFeeType;
    private FeeStructure feeStructure;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getCurrentTenant).thenReturn(TENANT_ID);

        tuitionFeeType = FeeType.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .name("TUITION")
                .description("Tuition Fee")
                .isRecurring(true)
                .frequency("MONTHLY")
                .isMandatory(true)
                .isActive(true)
                .build();

        feeStructure = FeeStructure.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .feeType(tuitionFeeType)
                .classId(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .dueDate(LocalDate.now().plusDays(30))
                .lateFee(new BigDecimal("100.00"))
                .discountPercentage(0.0)
                .academicYear("2025-2026")
                .term("Term 1")
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // =========================================================================
    // collectFee tests
    // =========================================================================

    @Nested
    @DisplayName("collectFee")
    class CollectFeeTests {

        @Test
        @DisplayName("should collect fee, set payment date, generate receipt number, and persist payment")
        void collectFee_shouldCreatePaymentAndGenerateReceipt() {
            // Arrange
            FeePayment inputPayment = FeePayment.builder()
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .studentName(STUDENT_NAME)
                    .amountPaid(new BigDecimal("5000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentMode(FeePayment.PaymentMode.CASH)
                    .collectedBy("admin-001")
                    .remarks("Full tuition payment")
                    .build();

            when(feePaymentRepository.save(any(FeePayment.class)))
                    .thenAnswer(invocation -> {
                        FeePayment payment = invocation.getArgument(0);
                        payment.setId(UUID.randomUUID());
                        payment.setCreatedAt(LocalDateTime.now());
                        return payment;
                    });

            // Act
            FeePayment result = feeService.collectFee(inputPayment);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(TENANT_ID, result.getTenantId());
            assertEquals(LocalDate.now(), result.getPaymentDate());

            // Receipt number should start with "RCP-"
            assertNotNull(result.getReceiptNumber());
            assertTrue(result.getReceiptNumber().startsWith("RCP-"),
                    "Receipt number should start with 'RCP-' prefix");

            assertEquals(STUDENT_ID, result.getStudentId());
            assertEquals(STUDENT_NAME, result.getStudentName());
            assertEquals(new BigDecimal("5000.00"), result.getAmountPaid());
            assertEquals(FeePayment.PaymentMode.CASH, result.getPaymentMode());

            verify(feePaymentRepository).save(any(FeePayment.class));
        }

        @Test
        @DisplayName("should set tenant ID from context and payment date to today")
        void collectFee_shouldSetTenantAndPaymentDate() {
            // Arrange
            FeePayment inputPayment = FeePayment.builder()
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(new BigDecimal("2500.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentMode(FeePayment.PaymentMode.UPI)
                    .build();

            when(feePaymentRepository.save(any(FeePayment.class)))
                    .thenAnswer(invocation -> {
                        FeePayment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID());
                        return saved;
                    });

            // Act
            FeePayment result = feeService.collectFee(inputPayment);

            // Assert
            assertEquals(TENANT_ID, result.getTenantId());
            assertEquals(LocalDate.now(), result.getPaymentDate());
        }

        @Test
        @DisplayName("should generate unique receipt numbers for different payments")
        void collectFee_shouldGenerateUniqueReceiptNumbers() throws InterruptedException {
            // Arrange
            FeePayment payment1 = FeePayment.builder()
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(new BigDecimal("1000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentMode(FeePayment.PaymentMode.CASH)
                    .build();

            FeePayment payment2 = FeePayment.builder()
                    .feeStructure(feeStructure)
                    .studentId(UUID.randomUUID())
                    .amountPaid(new BigDecimal("2000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentMode(FeePayment.PaymentMode.CARD)
                    .build();

            when(feePaymentRepository.save(any(FeePayment.class)))
                    .thenAnswer(invocation -> {
                        FeePayment p = invocation.getArgument(0);
                        p.setId(UUID.randomUUID());
                        return p;
                    });

            // Act
            FeePayment result1 = feeService.collectFee(payment1);
            // Small delay to ensure different millis timestamp
            Thread.sleep(5);
            FeePayment result2 = feeService.collectFee(payment2);

            // Assert
            assertNotNull(result1.getReceiptNumber());
            assertNotNull(result2.getReceiptNumber());
            assertNotEquals(result1.getReceiptNumber(), result2.getReceiptNumber(),
                    "Receipt numbers should be unique for different payments");
        }
    }

    // =========================================================================
    // getCollectionReport tests
    // =========================================================================

    @Nested
    @DisplayName("getCollectionReport")
    class GetCollectionReportTests {

        @Test
        @DisplayName("should return correct totals in collection report")
        void getCollectionReport_shouldReturnCorrectTotals() {
            // Arrange
            BigDecimal totalCollected = new BigDecimal("150000.00");
            BigDecimal totalPending = new BigDecimal("50000.00");
            Long overdueCount = 5L;
            BigDecimal monthlyCollection = new BigDecimal("30000.00");

            when(feePaymentRepository.getTotalCollectedByTenant(TENANT_ID))
                    .thenReturn(totalCollected);
            when(feePaymentRepository.getTotalPendingByTenant(TENANT_ID))
                    .thenReturn(totalPending);
            when(feePaymentRepository.countOverdueByTenant(TENANT_ID))
                    .thenReturn(overdueCount);
            when(feePaymentRepository.getCollectionBetweenDates(
                    eq(TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(monthlyCollection);

            // Act
            Map<String, Object> report = feeService.getCollectionReport();

            // Assert
            assertNotNull(report);
            assertEquals(totalCollected, report.get("totalCollected"));
            assertEquals(totalPending, report.get("totalPending"));
            assertEquals(overdueCount, report.get("overdueCount"));
            assertEquals(monthlyCollection, report.get("monthlyCollection"));
        }

        @Test
        @DisplayName("should return zero defaults when repository returns nulls")
        void getCollectionReport_withNullValues_shouldReturnZeroDefaults() {
            // Arrange
            when(feePaymentRepository.getTotalCollectedByTenant(TENANT_ID))
                    .thenReturn(null);
            when(feePaymentRepository.getTotalPendingByTenant(TENANT_ID))
                    .thenReturn(null);
            when(feePaymentRepository.countOverdueByTenant(TENANT_ID))
                    .thenReturn(null);
            when(feePaymentRepository.getCollectionBetweenDates(
                    eq(TENANT_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(null);

            // Act
            Map<String, Object> report = feeService.getCollectionReport();

            // Assert
            assertNotNull(report);
            assertEquals(BigDecimal.ZERO, report.get("totalCollected"));
            assertEquals(BigDecimal.ZERO, report.get("totalPending"));
            assertEquals(0L, report.get("overdueCount"));
            assertEquals(BigDecimal.ZERO, report.get("monthlyCollection"));
        }

        @Test
        @DisplayName("should query monthly collection with correct date range (first of month to today)")
        void getCollectionReport_shouldUseCorrectMonthlyDateRange() {
            // Arrange
            LocalDate expectedMonthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate expectedMonthEnd = LocalDate.now();

            when(feePaymentRepository.getTotalCollectedByTenant(TENANT_ID))
                    .thenReturn(BigDecimal.ZERO);
            when(feePaymentRepository.getTotalPendingByTenant(TENANT_ID))
                    .thenReturn(BigDecimal.ZERO);
            when(feePaymentRepository.countOverdueByTenant(TENANT_ID))
                    .thenReturn(0L);
            when(feePaymentRepository.getCollectionBetweenDates(
                    eq(TENANT_ID), eq(expectedMonthStart), eq(expectedMonthEnd)))
                    .thenReturn(new BigDecimal("10000.00"));

            // Act
            Map<String, Object> report = feeService.getCollectionReport();

            // Assert
            verify(feePaymentRepository).getCollectionBetweenDates(
                    TENANT_ID, expectedMonthStart, expectedMonthEnd);
            assertEquals(new BigDecimal("10000.00"), report.get("monthlyCollection"));
        }
    }

    // =========================================================================
    // getStudentPayments (getStudentFees) tests
    // =========================================================================

    @Nested
    @DisplayName("getStudentPayments (getStudentFees)")
    class GetStudentFeesTests {

        @Test
        @DisplayName("should return all payments for a specific student")
        void getStudentPayments_shouldReturnStudentSpecificPayments() {
            // Arrange
            FeePayment payment1 = FeePayment.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .studentName(STUDENT_NAME)
                    .amountPaid(new BigDecimal("5000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.PAID)
                    .paymentMode(FeePayment.PaymentMode.CASH)
                    .receiptNumber("RCP-001")
                    .paymentDate(LocalDate.now().minusDays(30))
                    .build();

            FeePayment payment2 = FeePayment.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .studentName(STUDENT_NAME)
                    .amountPaid(new BigDecimal("3000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .balanceAmount(new BigDecimal("2000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.PARTIAL)
                    .paymentMode(FeePayment.PaymentMode.UPI)
                    .receiptNumber("RCP-002")
                    .paymentDate(LocalDate.now().minusDays(5))
                    .build();

            when(feePaymentRepository.findByTenantIdAndStudentId(TENANT_ID, STUDENT_ID))
                    .thenReturn(List.of(payment1, payment2));

            // Act
            List<FeePayment> payments = feeService.getStudentPayments(STUDENT_ID);

            // Assert
            assertNotNull(payments);
            assertEquals(2, payments.size());

            // Verify all returned payments belong to the requested student
            assertTrue(payments.stream().allMatch(p -> p.getStudentId().equals(STUDENT_ID)),
                    "All payments should belong to the requested student");

            // Verify payment details
            FeePayment paidPayment = payments.stream()
                    .filter(p -> p.getPaymentStatus() == FeePayment.PaymentStatus.PAID)
                    .findFirst()
                    .orElseThrow();
            assertEquals(new BigDecimal("5000.00"), paidPayment.getAmountPaid());
            assertEquals(FeePayment.PaymentMode.CASH, paidPayment.getPaymentMode());

            FeePayment partialPayment = payments.stream()
                    .filter(p -> p.getPaymentStatus() == FeePayment.PaymentStatus.PARTIAL)
                    .findFirst()
                    .orElseThrow();
            assertEquals(new BigDecimal("3000.00"), partialPayment.getAmountPaid());
            assertEquals(new BigDecimal("2000.00"), partialPayment.getBalanceAmount());
        }

        @Test
        @DisplayName("should return empty list when student has no payments")
        void getStudentPayments_withNoPayments_shouldReturnEmptyList() {
            // Arrange
            UUID newStudentId = UUID.randomUUID();
            when(feePaymentRepository.findByTenantIdAndStudentId(TENANT_ID, newStudentId))
                    .thenReturn(Collections.emptyList());

            // Act
            List<FeePayment> payments = feeService.getStudentPayments(newStudentId);

            // Assert
            assertNotNull(payments);
            assertTrue(payments.isEmpty());
        }

        @Test
        @DisplayName("should query with correct tenant ID from context")
        void getStudentPayments_shouldUseTenantIdFromContext() {
            // Arrange
            when(feePaymentRepository.findByTenantIdAndStudentId(TENANT_ID, STUDENT_ID))
                    .thenReturn(Collections.emptyList());

            // Act
            feeService.getStudentPayments(STUDENT_ID);

            // Assert
            verify(feePaymentRepository).findByTenantIdAndStudentId(TENANT_ID, STUDENT_ID);
        }
    }

    // =========================================================================
    // getStudentPendingFees tests
    // =========================================================================

    @Nested
    @DisplayName("getStudentPendingFees")
    class GetStudentPendingFeesTests {

        @Test
        @DisplayName("should return only pending, partial, and overdue fees for a student")
        void getStudentPendingFees_shouldReturnOnlyOutstandingFees() {
            // Arrange
            FeePayment pendingPayment = FeePayment.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("5000.00"))
                    .balanceAmount(new BigDecimal("5000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.PENDING)
                    .build();

            FeePayment overduePayment = FeePayment.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("3000.00"))
                    .balanceAmount(new BigDecimal("3000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.OVERDUE)
                    .build();

            when(feePaymentRepository.findByTenantIdAndStudentIdAndPaymentStatusIn(
                    eq(TENANT_ID), eq(STUDENT_ID), anyList()))
                    .thenReturn(List.of(pendingPayment, overduePayment));

            // Act
            List<FeePayment> pendingFees = feeService.getStudentPendingFees(STUDENT_ID);

            // Assert
            assertNotNull(pendingFees);
            assertEquals(2, pendingFees.size());
            assertTrue(pendingFees.stream().noneMatch(
                    p -> p.getPaymentStatus() == FeePayment.PaymentStatus.PAID),
                    "No fully paid payments should be in the pending fees list");
        }
    }

    // =========================================================================
    // getPaymentById tests
    // =========================================================================

    @Nested
    @DisplayName("getPaymentById")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("should return payment when found by ID")
        void getPaymentById_shouldReturnPayment() {
            // Arrange
            UUID paymentId = UUID.randomUUID();
            FeePayment expected = FeePayment.builder()
                    .id(paymentId)
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(new BigDecimal("5000.00"))
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.PAID)
                    .receiptNumber("RCP-12345")
                    .build();

            when(feePaymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(expected));

            // Act
            FeePayment result = feeService.getPaymentById(paymentId);

            // Assert
            assertNotNull(result);
            assertEquals(paymentId, result.getId());
            assertEquals("RCP-12345", result.getReceiptNumber());
        }

        @Test
        @DisplayName("should throw NoSuchElementException when payment not found")
        void getPaymentById_withNonExistentId_shouldThrowException() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(feePaymentRepository.findById(nonExistentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NoSuchElementException exception = assertThrows(
                    NoSuchElementException.class,
                    () -> feeService.getPaymentById(nonExistentId));

            assertTrue(exception.getMessage().contains("Payment not found"));
        }
    }

    // =========================================================================
    // getOverduePayments tests
    // =========================================================================

    @Nested
    @DisplayName("getOverduePayments")
    class GetOverduePaymentsTests {

        @Test
        @DisplayName("should return only overdue payments for the tenant")
        void getOverduePayments_shouldReturnOverdueOnly() {
            // Arrange
            FeePayment overduePayment = FeePayment.builder()
                    .id(UUID.randomUUID())
                    .tenantId(TENANT_ID)
                    .feeStructure(feeStructure)
                    .studentId(STUDENT_ID)
                    .amountPaid(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("5000.00"))
                    .paymentStatus(FeePayment.PaymentStatus.OVERDUE)
                    .build();

            when(feePaymentRepository.findByTenantIdAndPaymentStatus(
                    TENANT_ID, FeePayment.PaymentStatus.OVERDUE))
                    .thenReturn(List.of(overduePayment));

            // Act
            List<FeePayment> overduePayments = feeService.getOverduePayments();

            // Assert
            assertNotNull(overduePayments);
            assertEquals(1, overduePayments.size());
            assertEquals(FeePayment.PaymentStatus.OVERDUE,
                    overduePayments.get(0).getPaymentStatus());
        }
    }
}
