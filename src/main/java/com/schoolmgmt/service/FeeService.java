package com.schoolmgmt.service;

import com.schoolmgmt.model.FeePayment;
import com.schoolmgmt.model.FeeStructure;
import com.schoolmgmt.model.FeeType;
import com.schoolmgmt.repository.FeePaymentRepository;
import com.schoolmgmt.repository.FeeStructureRepository;
import com.schoolmgmt.repository.FeeTypeRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {

    private final FeeTypeRepository feeTypeRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;

    // ===== Fee Type Operations =====

    @Transactional
    public FeeType createFeeType(FeeType feeType) {
        String tenantId = TenantContext.getCurrentTenant();
        feeType.setTenantId(tenantId);

        if (feeTypeRepository.existsByTenantIdAndName(tenantId, feeType.getName())) {
            throw new IllegalArgumentException("Fee type '" + feeType.getName() + "' already exists");
        }

        log.info("Creating fee type: {} for tenant: {}", feeType.getName(), tenantId);
        return feeTypeRepository.save(feeType);
    }

    @Transactional(readOnly = true)
    public List<FeeType> getFeeTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return feeTypeRepository.findByTenantIdOrderByName(tenantId);
    }

    @Transactional(readOnly = true)
    public List<FeeType> getActiveFeeTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return feeTypeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public FeeType updateFeeType(UUID id, FeeType updated) {
        FeeType existing = feeTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Fee type not found: " + id));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getIsRecurring() != null) existing.setIsRecurring(updated.getIsRecurring());
        if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
        if (updated.getIsMandatory() != null) existing.setIsMandatory(updated.getIsMandatory());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return feeTypeRepository.save(existing);
    }

    @Transactional
    public void deleteFeeType(UUID id) {
        feeTypeRepository.deleteById(id);
    }

    // ===== Fee Structure Operations =====

    @Transactional
    public FeeStructure createFeeStructure(FeeStructure structure) {
        String tenantId = TenantContext.getCurrentTenant();
        structure.setTenantId(tenantId);
        log.info("Creating fee structure for class {} with amount {}",
                structure.getClassId(), structure.getAmount());
        return feeStructureRepository.save(structure);
    }

    @Transactional(readOnly = true)
    public List<FeeStructure> getFeeStructures() {
        String tenantId = TenantContext.getCurrentTenant();
        return feeStructureRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional(readOnly = true)
    public List<FeeStructure> getFeeStructuresByClass(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        return feeStructureRepository.findByTenantIdAndClassIdAndIsActiveTrue(tenantId, classId);
    }

    @Transactional
    public FeeStructure updateFeeStructure(UUID id, FeeStructure updated) {
        FeeStructure existing = feeStructureRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Fee structure not found: " + id));

        if (updated.getAmount() != null) existing.setAmount(updated.getAmount());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getLateFee() != null) existing.setLateFee(updated.getLateFee());
        if (updated.getDiscountPercentage() != null) existing.setDiscountPercentage(updated.getDiscountPercentage());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return feeStructureRepository.save(existing);
    }

    // ===== Fee Payment Operations =====

    @Transactional
    public FeePayment collectFee(FeePayment payment) {
        String tenantId = TenantContext.getCurrentTenant();
        payment.setTenantId(tenantId);
        payment.setPaymentDate(LocalDate.now());

        // Generate receipt number
        payment.setReceiptNumber("RCP-" + System.currentTimeMillis());

        log.info("Collecting fee for student {} amount {}",
                payment.getStudentId(), payment.getAmountPaid());
        return feePaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<FeePayment> getStudentPayments(UUID studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return feePaymentRepository.findByTenantIdAndStudentId(tenantId, studentId);
    }

    @Transactional(readOnly = true)
    public List<FeePayment> getStudentPendingFees(UUID studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return feePaymentRepository.findByTenantIdAndStudentIdAndPaymentStatusIn(
                tenantId, studentId,
                List.of(FeePayment.PaymentStatus.PENDING, FeePayment.PaymentStatus.PARTIAL,
                        FeePayment.PaymentStatus.OVERDUE));
    }

    @Transactional(readOnly = true)
    public Page<FeePayment> getAllPayments(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return feePaymentRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public FeePayment getPaymentById(UUID paymentId) {
        return feePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCollectionReport() {
        String tenantId = TenantContext.getCurrentTenant();

        BigDecimal totalCollected = feePaymentRepository.getTotalCollectedByTenant(tenantId);
        BigDecimal totalPending = feePaymentRepository.getTotalPendingByTenant(tenantId);
        Long overdueCount = feePaymentRepository.countOverdueByTenant(tenantId);

        // This month's collection
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now();
        BigDecimal monthlyCollection = feePaymentRepository.getCollectionBetweenDates(
                tenantId, monthStart, monthEnd);

        Map<String, Object> report = new HashMap<>();
        report.put("totalCollected", totalCollected != null ? totalCollected : BigDecimal.ZERO);
        report.put("totalPending", totalPending != null ? totalPending : BigDecimal.ZERO);
        report.put("overdueCount", overdueCount != null ? overdueCount : 0L);
        report.put("monthlyCollection", monthlyCollection != null ? monthlyCollection : BigDecimal.ZERO);

        return report;
    }

    @Transactional(readOnly = true)
    public List<FeePayment> getOverduePayments() {
        String tenantId = TenantContext.getCurrentTenant();
        return feePaymentRepository.findByTenantIdAndPaymentStatus(
                tenantId, FeePayment.PaymentStatus.OVERDUE);
    }
}
