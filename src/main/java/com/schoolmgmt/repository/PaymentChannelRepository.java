package com.schoolmgmt.repository;

import com.schoolmgmt.model.PaymentChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentChannelRepository extends JpaRepository<PaymentChannel, UUID> {

    List<PaymentChannel> findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<PaymentChannel> findByTenantIdAndIsActiveTrueAndIsDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<PaymentChannel> findByIdAndTenantId(UUID id, String tenantId);
}
