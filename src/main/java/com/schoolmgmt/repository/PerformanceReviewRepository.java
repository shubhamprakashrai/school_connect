package com.schoolmgmt.repository;

import com.schoolmgmt.model.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID>, JpaSpecificationExecutor<PerformanceReview> {
    Page<PerformanceReview> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<PerformanceReview> findByStaffIdAndTenantIdAndIsActiveTrue(String staffId, String tenantId);
    Page<PerformanceReview> findByReviewPeriodAndTenantIdAndIsActiveTrue(String reviewPeriod, String tenantId, Pageable pageable);
    long countByTenantIdAndIsActiveTrue(String tenantId);
}
