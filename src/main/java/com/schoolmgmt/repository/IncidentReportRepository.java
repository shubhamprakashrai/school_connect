package com.schoolmgmt.repository;

import com.schoolmgmt.model.IncidentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, UUID> {

    Page<IncidentReport> findByTenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);

    List<IncidentReport> findByTenantIdAndStatusAndIsDeletedFalse(String tenantId, IncidentReport.IncidentStatus status);

    List<IncidentReport> findByTenantIdAndSeverityAndIsDeletedFalse(String tenantId, IncidentReport.Severity severity);

    @Query("SELECT i FROM IncidentReport i WHERE i.tenantId = :tenantId AND i.occurredAt BETWEEN :start AND :end AND i.isDeleted = false")
    List<IncidentReport> findByTenantIdAndDateRange(@Param("tenantId") String tenantId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    long countByTenantIdAndStatusAndIsDeletedFalse(String tenantId, IncidentReport.IncidentStatus status);

    long countByTenantIdAndSeverityAndIsDeletedFalse(String tenantId, IncidentReport.Severity severity);
}
