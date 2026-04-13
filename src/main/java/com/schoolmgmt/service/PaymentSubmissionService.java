package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.PaymentSubmissionRequest;
import com.schoolmgmt.model.PaymentSubmission;
import com.schoolmgmt.repository.PaymentChannelRepository;
import com.schoolmgmt.repository.PaymentSubmissionRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Manual payment submission + approval workflow (parent/student uploads
 * a screenshot → approver reviews). All operations tenant-scoped.
 *
 * Approval marks the submission APPROVED + records reviewer/timestamp.
 * Wiring to FeePayment / Razorpay reconciliation is left to a follow-up
 * (kept out so this service stays small and focused).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSubmissionService {

    private final PaymentSubmissionRepository submissionRepository;
    private final PaymentChannelRepository channelRepository;

    @Transactional
    public PaymentSubmission submit(PaymentSubmissionRequest req) {
        String tenantId = TenantContext.requireCurrentTenant();
        // Validate channel belongs to same tenant + is active.
        var channel = channelRepository
            .findByIdAndTenantId(req.getChannelId(), tenantId)
            .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .orElseThrow(() -> new IllegalArgumentException(
                "Payment channel not available: " + req.getChannelId()));

        PaymentSubmission s = PaymentSubmission.builder()
            .studentId(req.getStudentId())
            .feeId(req.getFeeId())
            .channelId(channel.getId())
            .amount(req.getAmount())
            .screenshotUrl(req.getScreenshotUrl())
            .remarks(req.getRemarks())
            .status(PaymentSubmission.SubmissionStatus.PENDING)
            .submittedAt(LocalDateTime.now())
            .build();
        log.info("Payment submission for student {} amount {} via channel {} (tenant {})",
            s.getStudentId(), s.getAmount(), channel.getLabel(), tenantId);
        return submissionRepository.save(s);
    }

    @Transactional(readOnly = true)
    public Page<PaymentSubmission> list(
            PaymentSubmission.SubmissionStatus status, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return status == null
            ? submissionRepository.findByTenantIdOrderBySubmittedAtDesc(tenantId, pageable)
            : submissionRepository.findByTenantIdAndStatusOrderBySubmittedAtDesc(
                tenantId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PaymentSubmission> listForStudent(UUID studentId, Pageable pageable) {
        return submissionRepository.findByTenantIdAndStudentIdOrderBySubmittedAtDesc(
            TenantContext.requireCurrentTenant(), studentId, pageable);
    }

    @Transactional(readOnly = true)
    public PaymentSubmission get(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        return submissionRepository.findByIdAndTenantId(id, tenantId)
            .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
            .orElseThrow(() ->
                new NoSuchElementException("Submission not found: " + id));
    }

    @Transactional
    public PaymentSubmission approve(UUID id) {
        PaymentSubmission s = get(id);
        if (s.getStatus() != PaymentSubmission.SubmissionStatus.PENDING) {
            throw new IllegalStateException(
                "Submission already " + s.getStatus());
        }
        s.setStatus(PaymentSubmission.SubmissionStatus.APPROVED);
        s.setReviewedBy(currentPrincipal());
        s.setReviewedAt(LocalDateTime.now());
        log.info("Approved submission {} by {}", id, s.getReviewedBy());
        // TODO: create FeePayment row + send notification (out of scope here).
        return submissionRepository.save(s);
    }

    @Transactional
    public PaymentSubmission reject(UUID id, String reason) {
        PaymentSubmission s = get(id);
        if (s.getStatus() != PaymentSubmission.SubmissionStatus.PENDING) {
            throw new IllegalStateException(
                "Submission already " + s.getStatus());
        }
        s.setStatus(PaymentSubmission.SubmissionStatus.REJECTED);
        s.setReviewedBy(currentPrincipal());
        s.setReviewedAt(LocalDateTime.now());
        s.setRejectionReason(reason);
        log.info("Rejected submission {} by {}: {}", id, s.getReviewedBy(), reason);
        return submissionRepository.save(s);
    }

    private String currentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "system" : auth.getName();
    }
}
