package com.schoolmgmt.repository;

import com.schoolmgmt.model.FeePayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, UUID> {

    List<FeePayment> findByTenantIdAndStudentId(String tenantId, UUID studentId);

    Page<FeePayment> findByTenantId(String tenantId, Pageable pageable);

    List<FeePayment> findByTenantIdAndPaymentStatus(
            String tenantId, FeePayment.PaymentStatus status);

    List<FeePayment> findByTenantIdAndPaymentDateBetween(
            String tenantId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(fp.amountPaid) FROM FeePayment fp WHERE fp.tenantId = :tenantId " +
           "AND fp.paymentStatus IN ('PAID', 'PARTIAL')")
    BigDecimal getTotalCollectedByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT SUM(fp.balanceAmount) FROM FeePayment fp WHERE fp.tenantId = :tenantId " +
           "AND fp.paymentStatus IN ('PENDING', 'PARTIAL', 'OVERDUE')")
    BigDecimal getTotalPendingByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT SUM(fp.amountPaid) FROM FeePayment fp WHERE fp.tenantId = :tenantId " +
           "AND fp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getCollectionBetweenDates(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(fp) FROM FeePayment fp WHERE fp.tenantId = :tenantId " +
           "AND fp.paymentStatus = 'OVERDUE'")
    Long countOverdueByTenant(@Param("tenantId") String tenantId);

    List<FeePayment> findByTenantIdAndStudentIdAndPaymentStatusIn(
            String tenantId, UUID studentId, List<FeePayment.PaymentStatus> statuses);
}
