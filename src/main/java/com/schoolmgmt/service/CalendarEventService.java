package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CalendarEventRequest;
import com.schoolmgmt.dto.response.CalendarEventResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.model.CalendarEvent;
import com.schoolmgmt.repository.AcademicYearRepository;
import com.schoolmgmt.repository.CalendarEventRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing tenant-specific calendar events.
 * Handles holidays, working days, and academic schedule.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final AcademicYearRepository academicYearRepository;

    /**
     * Creates a new calendar event.
     *
     * @param request the calendar event request
     * @return the created calendar event response
     */
    @Transactional
    public CalendarEventResponse createCalendarEvent(CalendarEventRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Creating calendar event: {} for tenant: {} on date: {}", 
                    request.getTitle(), tenantId, request.getEventDate());

            // Validate date is not in the past (optional, can be removed if historical entries allowed)
            if (request.getEventDate().isBefore(LocalDate.now().minusDays(30))) {
                log.warn("Attempt to create calendar event for old date: {}", request.getEventDate());
                // Not throwing exception, just warning - schools may need to backfill
            }

            // Check for existing event on same date
            if (calendarEventRepository.existsByTenantIdAndEventDate(tenantId, request.getEventDate())) {
                throw new BusinessException("Calendar event already exists for date: " + request.getEventDate());
            }

            // Validate academic year if provided
            if (request.getAcademicYearId() != null) {
                academicYearRepository.findById(request.getAcademicYearId())
                        .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            }

            // Determine isWorkingDay based on event type if not explicitly set
            Boolean isWorkingDay = request.getIsWorkingDay();
            if (isWorkingDay == null) {
                isWorkingDay = switch (request.getEventType()) {
                    case WORKING_DAY, EXAM, EVENT, SPORTS_DAY -> true;
                    case HOLIDAY, TEACHER_MEETING -> false;
                    case HALF_DAY, PARENT_TEACHER_MEETING -> true; // Partial working day
                };
            }

            CalendarEvent event = CalendarEvent.builder()
                    .eventDate(request.getEventDate())
                    .endDate(request.getEndDate())
                    .eventType(request.getEventType())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .isWorkingDay(isWorkingDay)
                    .academicYearId(request.getAcademicYearId())
                    .applicableSections(request.getApplicableSections())
                    .halfDay(request.getHalfDay() != null ? request.getHalfDay() : false)
                    .holidayType(request.getHolidayType())
                    .build();
            event.setTenantId(tenantId);

            CalendarEvent saved = calendarEventRepository.save(event);
            log.info("Calendar event created successfully: {} for tenant: {}", saved.getId(), tenantId);

            return toResponse(saved);

        } catch (BusinessException | ResourceNotFoundException e) {
            log.warn("Business exception while creating calendar event: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating calendar event: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating calendar event", e);
        }
    }

    /**
     * Gets calendar event by ID.
     *
     * @param id the calendar event ID
     * @return the calendar event response
     */
    @Transactional(readOnly = true)
    public CalendarEventResponse getCalendarEventById(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching calendar event: {} for tenant: {}", id, tenantId);

            CalendarEvent event = calendarEventRepository.findById(id)
                    .filter(e -> e.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));

            return toResponse(event);

        } catch (ResourceNotFoundException e) {
            log.warn("Calendar event not found: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching calendar event {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching calendar event", e);
        }
    }

    /**
     * Gets calendar event for a specific date.
     *
     * @param date the date to check
     * @return the calendar event response, or default working day if no event exists
     */
    @Transactional(readOnly = true)
    public CalendarEventResponse getCalendarEventByDate(LocalDate date) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching calendar event for date: {} and tenant: {}", date, tenantId);

            return calendarEventRepository.findByTenantIdAndEventDate(tenantId, date)
                    .map(this::toResponse)
                    .orElseGet(() -> CalendarEventResponse.builder()
                            .eventDate(date)
                            .eventType(CalendarEvent.EventType.WORKING_DAY)
                            .title("Working Day")
                            .isWorkingDay(true)
                            .build());

        } catch (Exception e) {
            log.error("Error fetching calendar event for date {}: {}", date, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching calendar event", e);
        }
    }

    /**
     * Gets all calendar events for a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of calendar event responses
     */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getCalendarEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching calendar events from {} to {} for tenant: {}", startDate, endDate, tenantId);

            if (endDate.isBefore(startDate)) {
                throw new BusinessException("End date must be after start date");
            }

            List<CalendarEvent> events = calendarEventRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);

            return events.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching calendar events: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching calendar events", e);
        }
    }

    /**
     * Gets all calendar events for an academic year.
     *
     * @param academicYearId the academic year ID
     * @return list of calendar event responses
     */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getCalendarEventsByAcademicYear(UUID academicYearId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching calendar events for academic year: {} and tenant: {}", academicYearId, tenantId);

            // Verify academic year exists
            academicYearRepository.findById(academicYearId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", academicYearId));

            List<CalendarEvent> events = calendarEventRepository.findByTenantIdAndAcademicYearId(tenantId, academicYearId);

            return events.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching calendar events for academic year: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching calendar events", e);
        }
    }

    /**
     * Updates a calendar event.
     *
     * @param id the calendar event ID
     * @param request the update request
     * @return the updated calendar event response
     */
    @Transactional
    public CalendarEventResponse updateCalendarEvent(UUID id, CalendarEventRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Updating calendar event: {} for tenant: {}", id, tenantId);

            CalendarEvent event = calendarEventRepository.findById(id)
                    .filter(e -> e.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));

            // Check if date changed and new date already has an event (excluding current)
            if (!event.getEventDate().equals(request.getEventDate())) {
                if (calendarEventRepository.existsByTenantIdAndEventDate(tenantId, request.getEventDate())) {
                    throw new BusinessException("Calendar event already exists for date: " + request.getEventDate());
                }
            }

            // Validate academic year if provided
            if (request.getAcademicYearId() != null && 
                (event.getAcademicYearId() == null || !event.getAcademicYearId().equals(request.getAcademicYearId()))) {
                academicYearRepository.findById(request.getAcademicYearId())
                        .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            }

            // Determine isWorkingDay based on event type if not explicitly set
            Boolean isWorkingDay = request.getIsWorkingDay();
            if (isWorkingDay == null) {
                isWorkingDay = switch (request.getEventType()) {
                    case WORKING_DAY, EXAM, EVENT, SPORTS_DAY -> true;
                    case HOLIDAY, TEACHER_MEETING -> false;
                    case HALF_DAY, PARENT_TEACHER_MEETING -> true;
                };
            }

            event.setEventDate(request.getEventDate());
            event.setEndDate(request.getEndDate());
            event.setEventType(request.getEventType());
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setIsWorkingDay(isWorkingDay);
            event.setAcademicYearId(request.getAcademicYearId());
            event.setApplicableSections(request.getApplicableSections());
            event.setHalfDay(request.getHalfDay() != null ? request.getHalfDay() : false);
            event.setHolidayType(request.getHolidayType());

            CalendarEvent updated = calendarEventRepository.save(event);
            log.info("Calendar event updated successfully: {} for tenant: {}", updated.getId(), tenantId);

            return toResponse(updated);

        } catch (ResourceNotFoundException | BusinessException e) {
            log.warn("Exception while updating calendar event: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating calendar event {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while updating calendar event", e);
        }
    }

    /**
     * Deletes a calendar event.
     *
     * @param id the calendar event ID
     */
    @Transactional
    public void deleteCalendarEvent(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Deleting calendar event: {} for tenant: {}", id, tenantId);

            CalendarEvent event = calendarEventRepository.findById(id)
                    .filter(e -> e.getTenantId().equals(tenantId))
                    .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id));

            calendarEventRepository.delete(event);
            log.info("Calendar event deleted successfully: {} for tenant: {}", id, tenantId);

        } catch (ResourceNotFoundException e) {
            log.warn("Calendar event not found for deletion: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting calendar event {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while deleting calendar event", e);
        }
    }

    /**
     * Checks if a date is a working day for the tenant.
     *
     * @param date the date to check
     * @return true if working day, false otherwise
     */
    public boolean isWorkingDay(LocalDate date) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                log.warn("No tenant context available, defaulting to working day for date: {}", date);
                return true; // Default to working day if no tenant context
            }
            Boolean isWorking = calendarEventRepository.isWorkingDay(tenantId, date);
            return isWorking != null ? isWorking : true; // Default to working day if no event
        } catch (Exception e) {
            log.error("Error checking working day for date {}: {}", date, e.getMessage(), e);
            return true; // Default to working day on error
        }
    }

    /**
     * Converts CalendarEvent entity to CalendarEventResponse DTO.
     */
    private CalendarEventResponse toResponse(CalendarEvent event) {
        String academicYearName = null;
        if (event.getAcademicYearId() != null) {
            academicYearName = academicYearRepository.findById(event.getAcademicYearId())
                    .map(AcademicYear::getName)
                    .orElse(null);
        }

        return CalendarEventResponse.builder()
                .id(event.getId())
                .eventDate(event.getEventDate())
                .endDate(event.getEndDate())
                .eventType(event.getEventType())
                .title(event.getTitle())
                .description(event.getDescription())
                .isWorkingDay(event.getIsWorkingDay())
                .academicYearId(event.getAcademicYearId())
                .academicYearName(academicYearName)
                .applicableSections(event.getApplicableSections())
                .halfDay(event.getHalfDay())
                .holidayType(event.getHolidayType())
                .createdAt(event.getCreatedAt() != null ? event.getCreatedAt().toLocalDate() : null)
                .updatedAt(event.getUpdatedAt() != null ? event.getUpdatedAt().toLocalDate() : null)
                .build();
    }
}
