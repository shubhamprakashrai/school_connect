package com.schoolmgmt.repository;

import com.schoolmgmt.model.SchoolSubscription;
import com.schoolmgmt.model.SchoolSubscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, UUID> {
    Optional<SchoolSubscription> findByTenantId(String tenantId);
    List<SchoolSubscription> findByStatus(SubscriptionStatus status);
    List<SchoolSubscription> findByNextBillingDateAndAutoRenewTrue(LocalDate date);

    @Query("SELECT s FROM SchoolSubscription s WHERE s.expiresAt < :today AND s.status NOT IN ('SUSPENDED', 'CANCELLED')")
    List<SchoolSubscription> findExpiredSubscriptions(@Param("today") LocalDate today);

    List<SchoolSubscription> findByStatusIn(List<SubscriptionStatus> statuses);
}
