package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.request.CalendarEventRequest;
import com.schoolmgmt.dto.response.CalendarEventResponse;
import com.schoolmgmt.service.CalendarEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for tenant calendar event management.
 * Handles holidays, working days, and academic schedule.
 */
@RestController
@RequestMapping("/calendar-events")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Calendar Management", description = "Tenant calendar and holiday management APIs")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @PostMapping
    @Operation(summary = "Create calendar event", description = "Create a new calendar event (holiday, working day, exam, etc.)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> createCalendarEvent(
            @Valid @RequestBody CalendarEventRequest request) {
        log.info("REST request to create calendar event: {}", request.getTitle());
        CalendarEventResponse response = calendarEventService.createCalendarEvent(request);
        return new ResponseEntity<>(ApiResponse.success("Calendar event created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get calendar event by ID", description = "Get calendar event details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STAFF')")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> getCalendarEventById(
            @Parameter(description = "Calendar Event ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to get calendar event: {}", id);
        CalendarEventResponse response = calendarEventService.getCalendarEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Calendar event fetched successfully", response));
    }

    @GetMapping("/by-date")
    @Operation(summary = "Get calendar event by date", description = "Get calendar event for a specific date (defaults to working day if no event)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STAFF')")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> getCalendarEventByDate(
            @Parameter(description = "Date (YYYY-MM-DD)", required = true, example = "2025-01-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to get calendar event for date: {}", date);
        CalendarEventResponse response = calendarEventService.getCalendarEventByDate(date);
        return ResponseEntity.ok(ApiResponse.success("Calendar event fetched successfully", response));
    }

    @GetMapping("/by-range")
    @Operation(summary = "Get calendar events by date range", description = "Get all calendar events within a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> getCalendarEventsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2025-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to get calendar events from {} to {}", startDate, endDate);
        List<CalendarEventResponse> responses = calendarEventService.getCalendarEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Calendar events fetched successfully", responses));
    }

    @GetMapping("/by-academic-year/{academicYearId}")
    @Operation(summary = "Get calendar events by academic year", description = "Get all calendar events for a specific academic year")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> getCalendarEventsByAcademicYear(
            @Parameter(description = "Academic Year ID", required = true)
            @PathVariable UUID academicYearId) {
        log.info("REST request to get calendar events for academic year: {}", academicYearId);
        List<CalendarEventResponse> responses = calendarEventService.getCalendarEventsByAcademicYear(academicYearId);
        return ResponseEntity.ok(ApiResponse.success("Calendar events fetched successfully", responses));
    }

    @GetMapping("/is-working-day")
    @Operation(summary = "Check if working day", description = "Check if a specific date is a working day for the tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'STAFF')")
    public ResponseEntity<ApiResponse<Boolean>> isWorkingDay(
            @Parameter(description = "Date to check (YYYY-MM-DD)", required = true, example = "2025-01-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to check if working day: {}", date);
        boolean isWorking = calendarEventService.isWorkingDay(date);
        String message = isWorking ? "Date is a working day" : "Date is not a working day";
        return ResponseEntity.ok(ApiResponse.success(message, isWorking));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update calendar event", description = "Update an existing calendar event")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> updateCalendarEvent(
            @Parameter(description = "Calendar Event ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody CalendarEventRequest request) {
        log.info("REST request to update calendar event: {}", id);
        CalendarEventResponse response = calendarEventService.updateCalendarEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Calendar event updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete calendar event", description = "Delete a calendar event")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCalendarEvent(
            @Parameter(description = "Calendar Event ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to delete calendar event: {}", id);
        calendarEventService.deleteCalendarEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Calendar event deleted successfully", null));
    }
}
