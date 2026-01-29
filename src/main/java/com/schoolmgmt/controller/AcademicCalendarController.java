package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.CreateAcademicEventRequest;
import com.schoolmgmt.dto.request.UpdateAcademicEventRequest;
import com.schoolmgmt.dto.response.AcademicEventResponse;
import com.schoolmgmt.service.AcademicCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for academic calendar and event management operations.
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Academic Calendar", description = "Academic calendar and event management APIs")
public class AcademicCalendarController {

    private final AcademicCalendarService calendarService;

    @PostMapping
    @Operation(summary = "Create academic event", description = "Create a new academic calendar event")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AcademicEventResponse> createEvent(@Valid @RequestBody CreateAcademicEventRequest request) {
        log.info("Creating academic event: {}", request.getTitle());
        AcademicEventResponse response = calendarService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update academic event", description = "Update an existing academic calendar event")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AcademicEventResponse> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateAcademicEventRequest request) {
        log.info("Updating academic event: {}", eventId);
        AcademicEventResponse response = calendarService.updateEvent(eventId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete academic event", description = "Soft delete an academic calendar event")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteEvent(@PathVariable UUID eventId) {
        log.info("Deleting academic event: {}", eventId);
        calendarService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("Academic event deleted successfully"));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get academic event by ID", description = "Get academic event details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<AcademicEventResponse> getEventById(@PathVariable UUID eventId) {
        log.info("Fetching academic event: {}", eventId);
        AcademicEventResponse response = calendarService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all academic events", description = "Get all academic events with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<Page<AcademicEventResponse>> getAllEvents(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all academic events");
        Page<AcademicEventResponse> events = calendarService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/range")
    @Operation(summary = "Get events by date range", description = "Get academic events within a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<AcademicEventResponse>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("Fetching events for date range: {} to {}", start, end);
        List<AcademicEventResponse> events = calendarService.getEventsByDateRange(start, end);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/month")
    @Operation(summary = "Get events by month", description = "Get academic events for a specific month")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<AcademicEventResponse>> getEventsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Fetching events for month: {}/{}", year, month);
        List<AcademicEventResponse> events = calendarService.getEventsByMonth(year, month);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events", description = "Get upcoming academic events")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<AcademicEventResponse>> getUpcomingEvents(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching upcoming events, limit: {}", limit);
        List<AcademicEventResponse> events = calendarService.getUpcomingEvents(limit);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/holidays")
    @Operation(summary = "Get holidays", description = "Get holidays for an academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<AcademicEventResponse>> getHolidays(
            @RequestParam String academicYear) {
        log.info("Fetching holidays for academic year: {}", academicYear);
        List<AcademicEventResponse> holidays = calendarService.getHolidays(academicYear);
        return ResponseEntity.ok(holidays);
    }
}
