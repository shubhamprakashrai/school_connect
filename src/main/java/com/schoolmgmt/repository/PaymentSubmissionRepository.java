package com.schoolmgmt.repository;

import com.schoolmgmt.model.PaymentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentSubmissionRepository extends JpaRepository<PaymentSubmission, UUID> {

    Page<PaymentSubmission> findByTenantIdOrderBySubmittedAtDesc(
            String tenantId, Pageable pageable);

    Page<PaymentSubmission> findByTenantIdAndStatusOrderBySubmittedAtDesc(
            String tenantId,
            PaymentSubmission.SubmissionStatus status,
            Pageable pageable);

    Page<PaymentSubmission> findByTenantIdAndStudentIdOrderBySubmittedAtDesc(
            String tenantId, UUID studentId, Pageable pageable);

    Optional<PaymentSubmission> findByIdAndTenantId(UUID id, String tenantId);

    long countByTenantIdAndStatus(
            String tenantId, PaymentSubmission.SubmissionStatus status);
}
