package com.schoolmgmt.repository;

import com.schoolmgmt.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CalendarEvent entity operations.
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    /**
     * Find calendar event for a specific date and tenant.
     */
    @Query("SELECT c FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate = :date")
    Optional<CalendarEvent> findByTenantIdAndEventDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    /**
     * Find all calendar events for a date range.
     */
    @Query("SELECT c FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate BETWEEN :startDate AND :endDate ORDER BY c.eventDate")
    List<CalendarEvent> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Check if a date is a working day for the tenant.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN MAX(c.isWorkingDay) ELSE true END FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate = :date")
    Boolean isWorkingDay(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    /**
     * Find all holidays in a date range.
     */
    @Query("SELECT c FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate BETWEEN :startDate AND :endDate AND c.isWorkingDay = false")
    List<CalendarEvent> findHolidaysInRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find events by type for a tenant.
     */
    @Query("SELECT c FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventType = :eventType ORDER BY c.eventDate")
    List<CalendarEvent> findByTenantIdAndEventType(@Param("tenantId") String tenantId, @Param("eventType") CalendarEvent.EventType eventType);

    /**
     * Check if a calendar event exists for the given date and tenant.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate = :date")
    boolean existsByTenantIdAndEventDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    /**
     * Find all calendar events for a specific academic year.
     */
    @Query("SELECT c FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.academicYearId = :academicYearId ORDER BY c.eventDate")
    List<CalendarEvent> findByTenantIdAndAcademicYearId(@Param("tenantId") String tenantId, @Param("academicYearId") UUID academicYearId);

    /**
     * Count working days in a date range.
     */
    @Query("SELECT COUNT(c) FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate BETWEEN :startDate AND :endDate AND c.isWorkingDay = true")
    Long countWorkingDaysInRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Count holidays in a date range.
     */
    @Query("SELECT COUNT(c) FROM CalendarEvent c WHERE c.tenantId = :tenantId AND c.eventDate BETWEEN :startDate AND :endDate AND c.isWorkingDay = false")
    Long countHolidaysInRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
