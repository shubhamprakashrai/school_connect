package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.PaymentTransaction;
import com.schoolmgmt.repository.PaymentTransactionRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentTransactionService {

    private final PaymentTransactionRepository repository;

    public PaymentTransaction create(PaymentTransaction transaction) {
        String tenantId = TenantContext.requireCurrentTenant();
        transaction.setTenantId(tenantId);
        if (transaction.getReceiptNumber() == null) {
            transaction.setReceiptNumber("RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        PaymentTransaction saved = repository.save(transaction);
        log.info("Payment transaction created: {} amount: {}", saved.getId(), saved.getAmount());
        return saved;
    }

    @Transactional(readOnly = true)
    public PaymentTransaction getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        PaymentTransaction tx = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", id));
        if (!tx.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("PaymentTransaction", "id", id);
        }
        return tx;
    }

    @Transactional(readOnly = true)
    public Page<PaymentTransaction> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> getByStudentId(String studentId) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByStudentIdAndTenantIdAndIsActiveTrue(studentId, tenantId);
    }

    public PaymentTransaction update(UUID id, PaymentTransaction updates) {
        PaymentTransaction existing = getById(id);
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getTransactionId() != null) existing.setTransactionId(updates.getTransactionId());
        if (updates.getGatewayReference() != null) existing.setGatewayReference(updates.getGatewayReference());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        PaymentTransaction tx = getById(id);
        tx.softDelete(TenantContext.requireCurrentTenant());
        repository.save(tx);
        log.info("Payment transaction deleted: {}", id);
    }
}
