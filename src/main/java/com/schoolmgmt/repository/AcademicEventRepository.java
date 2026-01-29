package com.schoolmgmt.repository;

import com.schoolmgmt.model.AcademicEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AcademicEvent entity operations.
 */
@Repository
public interface AcademicEventRepository extends JpaRepository<AcademicEvent, UUID>, JpaSpecificationExecutor<AcademicEvent> {

    /**
     * Find events by tenant and date range
     */
    List<AcademicEvent> findByTenantIdAndStartDateBetweenAndIsDeletedFalse(
            String tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * Find events by tenant and event type
     */
    List<AcademicEvent> findByTenantIdAndEventTypeAndIsDeletedFalse(
            String tenantId, AcademicEvent.EventType eventType);

    /**
     * Find events by tenant and target audience
     */
    List<AcademicEvent> findByTenantIdAndTargetAudienceAndIsDeletedFalse(
            String tenantId, AcademicEvent.TargetAudience targetAudience);

    /**
     * Find upcoming events (start date >= today)
     */
    @Query("SELECT e FROM AcademicEvent e WHERE e.tenantId = :tenantId " +
           "AND e.startDate >= :today AND e.isDeleted = false " +
           "ORDER BY e.startDate ASC")
    List<AcademicEvent> findUpcomingEvents(
            @Param("tenantId") String tenantId,
            @Param("today") LocalDate today,
            Pageable pageable);

    /**
     * Find events that overlap with a date range (events whose range intersects the query range)
     */
    @Query("SELECT e FROM AcademicEvent e WHERE e.tenantId = :tenantId " +
           "AND e.startDate <= :endDate AND e.endDate >= :startDate " +
           "AND e.isDeleted = false ORDER BY e.startDate ASC")
    List<AcademicEvent> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find holidays for an academic year
     */
    @Query("SELECT e FROM AcademicEvent e WHERE e.tenantId = :tenantId " +
           "AND e.eventType = 'HOLIDAY' AND e.academicYear = :academicYear " +
           "AND e.isDeleted = false ORDER BY e.startDate ASC")
    List<AcademicEvent> findHolidaysByAcademicYear(
            @Param("tenantId") String tenantId,
            @Param("academicYear") String academicYear);

    /**
     * Paginated list of events by tenant
     */
    Page<AcademicEvent> findByTenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);

    /**
     * Find event by ID and tenant
     */
    Optional<AcademicEvent> findByIdAndTenantIdAndIsDeletedFalse(UUID id, String tenantId);
}
