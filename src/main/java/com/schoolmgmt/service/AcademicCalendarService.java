package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateAcademicEventRequest;
import com.schoolmgmt.dto.request.UpdateAcademicEventRequest;
import com.schoolmgmt.dto.response.AcademicEventResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicEvent;
import com.schoolmgmt.repository.AcademicEventRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for academic calendar and event management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademicCalendarService {

    private final AcademicEventRepository eventRepository;

    /**
     * Create a new academic event
     */
    public AcademicEventResponse createEvent(CreateAcademicEventRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        AcademicEvent event = AcademicEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(AcademicEvent.EventType.valueOf(request.getEventType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isAllDay(request.getIsAllDay() != null ? request.getIsAllDay() : true)
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .color(request.getColor())
                .targetAudience(request.getTargetAudience() != null
                        ? AcademicEvent.TargetAudience.valueOf(request.getTargetAudience())
                        : AcademicEvent.TargetAudience.ALL)
                .classId(request.getClassId() != null ? UUID.fromString(request.getClassId()) : null)
                .academicYear(request.getAcademicYear())
                .build();

        event.setTenantId(tenantId);

        AcademicEvent savedEvent = eventRepository.save(event);
        log.info("Academic event created: {} - {} in tenant: {}", savedEvent.getId(), savedEvent.getTitle(), tenantId);

        return toEventResponse(savedEvent);
    }

    /**
     * Update an existing academic event
     */
    public AcademicEventResponse updateEvent(UUID eventId, UpdateAcademicEventRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicEvent event = eventRepository.findByIdAndTenantIdAndIsDeletedFalse(eventId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicEvent", "id", eventId));

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventType() != null) {
            event.setEventType(AcademicEvent.EventType.valueOf(request.getEventType()));
        }
        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }
        if (request.getIsAllDay() != null) {
            event.setIsAllDay(request.getIsAllDay());
        }
        if (request.getIsRecurring() != null) {
            event.setIsRecurring(request.getIsRecurring());
        }
        if (request.getColor() != null) {
            event.setColor(request.getColor());
        }
        if (request.getTargetAudience() != null) {
            event.setTargetAudience(AcademicEvent.TargetAudience.valueOf(request.getTargetAudience()));
        }
        if (request.getClassId() != null) {
            event.setClassId(UUID.fromString(request.getClassId()));
        }
        if (request.getAcademicYear() != null) {
            event.setAcademicYear(request.getAcademicYear());
        }

        // Validate dates after update
        if (event.getEndDate().isBefore(event.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        AcademicEvent updatedEvent = eventRepository.save(event);
        log.info("Academic event updated: {} - {}", updatedEvent.getId(), updatedEvent.getTitle());

        return toEventResponse(updatedEvent);
    }

    /**
     * Delete an academic event (soft delete)
     */
    public void deleteEvent(UUID eventId) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicEvent event = eventRepository.findByIdAndTenantIdAndIsDeletedFalse(eventId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicEvent", "id", eventId));

        event.softDelete(tenantId);
        eventRepository.save(event);

        log.info("Academic event soft deleted: {}", eventId);
    }

    /**
     * Get event by ID
     */
    @Transactional(readOnly = true)
    public AcademicEventResponse getEventById(UUID eventId) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicEvent event = eventRepository.findByIdAndTenantIdAndIsDeletedFalse(eventId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicEvent", "id", eventId));

        return toEventResponse(event);
    }

    /**
     * Get all events with pagination
     */
    @Transactional(readOnly = true)
    public Page<AcademicEventResponse> getAllEvents(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();

        Page<AcademicEvent> events = eventRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
        return events.map(this::toEventResponse);
    }

    /**
     * Get events by date range
     */
    @Transactional(readOnly = true)
    public List<AcademicEventResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.requireCurrentTenant();

        List<AcademicEvent> events = eventRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    /**
     * Get events by month
     */
    @Transactional(readOnly = true)
    public List<AcademicEventResponse> getEventsByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return getEventsByDateRange(startDate, endDate);
    }

    /**
     * Get upcoming events
     */
    @Transactional(readOnly = true)
    public List<AcademicEventResponse> getUpcomingEvents(int limit) {
        String tenantId = TenantContext.requireCurrentTenant();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "startDate"));
        List<AcademicEvent> events = eventRepository.findUpcomingEvents(tenantId, LocalDate.now(), pageable);
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    /**
     * Get holidays for an academic year
     */
    @Transactional(readOnly = true)
    public List<AcademicEventResponse> getHolidays(String academicYear) {
        String tenantId = TenantContext.requireCurrentTenant();

        List<AcademicEvent> holidays = eventRepository.findHolidaysByAcademicYear(tenantId, academicYear);
        return holidays.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    /**
     * Get events by type
     */
    @Transactional(readOnly = true)
    public List<AcademicEventResponse> getEventsByType(String eventType) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicEvent.EventType type = AcademicEvent.EventType.valueOf(eventType);
        List<AcademicEvent> events = eventRepository.findByTenantIdAndEventTypeAndIsDeletedFalse(tenantId, type);
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    /**
     * Convert AcademicEvent entity to AcademicEventResponse DTO
     */
    private AcademicEventResponse toEventResponse(AcademicEvent event) {
        return AcademicEventResponse.builder()
                .id(event.getId().toString())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType().name())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isAllDay(event.getIsAllDay())
                .isRecurring(event.getIsRecurring())
                .color(event.getColor())
                .targetAudience(event.getTargetAudience().name())
                .classId(event.getClassId() != null ? event.getClassId().toString() : null)
                .academicYear(event.getAcademicYear())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
