package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.PaymentChannelRequest;
import com.schoolmgmt.model.PaymentChannel;
import com.schoolmgmt.repository.PaymentChannelRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * CRUD for tenant-owned payment channels (QR / BANK / UPI). All reads
 * and writes are tenant-scoped via {@link TenantContext}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentChannelService {

    private final PaymentChannelRepository repository;

    @Transactional(readOnly = true)
    public List<PaymentChannel> listAll() {
        return repository.findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(
            TenantContext.requireCurrentTenant());
    }

    @Transactional(readOnly = true)
    public List<PaymentChannel> listActive() {
        return repository
            .findByTenantIdAndIsActiveTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                TenantContext.requireCurrentTenant());
    }

    @Transactional(readOnly = true)
    public PaymentChannel get(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByIdAndTenantId(id, tenantId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .orElseThrow(() ->
                new NoSuchElementException("Payment channel not found: " + id));
    }

    @Transactional
    public PaymentChannel create(PaymentChannelRequest req) {
        PaymentChannel channel = PaymentChannel.builder()
            .channelType(req.getChannelType())
            .label(req.getLabel())
            .qrImageUrl(req.getQrImageUrl())
            .bankName(req.getBankName())
            .accountNumber(req.getAccountNumber())
            .ifsc(req.getIfsc())
            .accountHolder(req.getAccountHolder())
            .upiId(req.getUpiId())
            .instructions(req.getInstructions())
            .isActive(req.getIsActive() == null ? true : req.getIsActive())
            .build();
        log.info("Creating payment channel '{}' for tenant {}",
            channel.getLabel(), TenantContext.getCurrentTenant());
        return repository.save(channel);
    }

    @Transactional
    public PaymentChannel update(UUID id, PaymentChannelRequest req) {
        PaymentChannel c = get(id);
        if (req.getChannelType() != null) c.setChannelType(req.getChannelType());
        if (req.getLabel() != null) c.setLabel(req.getLabel());
        if (req.getQrImageUrl() != null) c.setQrImageUrl(req.getQrImageUrl());
        if (req.getBankName() != null) c.setBankName(req.getBankName());
        if (req.getAccountNumber() != null) c.setAccountNumber(req.getAccountNumber());
        if (req.getIfsc() != null) c.setIfsc(req.getIfsc());
        if (req.getAccountHolder() != null) c.setAccountHolder(req.getAccountHolder());
        if (req.getUpiId() != null) c.setUpiId(req.getUpiId());
        if (req.getInstructions() != null) c.setInstructions(req.getInstructions());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());
        return repository.save(c);
    }

    @Transactional
    public void delete(UUID id) {
        PaymentChannel c = get(id);
        c.softDelete("system");
        repository.save(c);
    }
}
