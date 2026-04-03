package com.schoolmgmt.repository;

import com.schoolmgmt.model.SubscriptionInvoice;
import com.schoolmgmt.model.SubscriptionInvoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, UUID> {
    List<SubscriptionInvoice> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Page<SubscriptionInvoice> findByTenantId(String tenantId, Pageable pageable);
    Page<SubscriptionInvoice> findByStatus(InvoiceStatus status, Pageable pageable);
    Optional<SubscriptionInvoice> findByRazorpayOrderId(String razorpayOrderId);
    Optional<SubscriptionInvoice> findByRazorpayPaymentId(String razorpayPaymentId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM SubscriptionInvoice i WHERE i.status = 'PAID'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM SubscriptionInvoice i WHERE i.status IN ('PENDING', 'OVERDUE')")
    BigDecimal getTotalOutstanding();

    @Query("SELECT MAX(i.invoiceNumber) FROM SubscriptionInvoice i")
    String getLastInvoiceNumber();

    long countByStatus(InvoiceStatus status);
}
