package com.schoolmgmt.repository;

import com.schoolmgmt.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodRepository extends JpaRepository<Period, UUID> {

    List<Period> findByTenantIdOrderByPeriodNumber(String tenantId);

    List<Period> findByTenantIdAndIsActiveTrue(String tenantId);

    Optional<Period> findByTenantIdAndPeriodNumber(String tenantId, Integer periodNumber);

    boolean existsByTenantIdAndPeriodNumber(String tenantId, Integer periodNumber);

    List<Period> findByTenantIdAndIsBreakFalseOrderByPeriodNumber(String tenantId);
}
