package com.schoolmgmt.repository;

import com.schoolmgmt.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    List<SubscriptionPlan> findByIsActiveTrue();
    List<SubscriptionPlan> findByTenantIdAndIsActiveTrue(String tenantId);
    List<SubscriptionPlan> findByTenantIdIsNullAndIsActiveTrue();
    boolean existsByNameAndTenantId(String name, String tenantId);
}
