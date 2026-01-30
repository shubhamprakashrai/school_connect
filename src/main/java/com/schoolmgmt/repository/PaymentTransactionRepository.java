package com.schoolmgmt.repository;

import com.schoolmgmt.model.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID>, JpaSpecificationExecutor<PaymentTransaction> {
    Page<PaymentTransaction> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<PaymentTransaction> findByStudentIdAndTenantIdAndIsActiveTrue(String studentId, String tenantId);
    Optional<PaymentTransaction> findByTransactionIdAndTenantId(String transactionId, String tenantId);
    Page<PaymentTransaction> findByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
    long countByStatusAndTenantIdAndIsActiveTrue(String status, String tenantId);
}
