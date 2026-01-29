package com.schoolmgmt.controller;

import com.schoolmgmt.model.Period;
import com.schoolmgmt.model.TimetableEntry;
import com.schoolmgmt.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Timetable Management", description = "APIs for managing timetables and periods")
public class TimetableController {

    private final TimetableService timetableService;

    // ===== Period Endpoints =====

    @PostMapping("/periods")
    @Operation(summary = "Create period", description = "Create a new period slot")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Period> createPeriod(@Valid @RequestBody Period period) {
        log.info("Creating period: {}", period.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.createPeriod(period));
    }

    @GetMapping("/periods")
    @Operation(summary = "Get all periods", description = "Get all period slots for the tenant")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Period>> getAllPeriods() {
        return ResponseEntity.ok(timetableService.getAllPeriods());
    }

    @GetMapping("/periods/active")
    @Operation(summary = "Get active periods", description = "Get only active period slots")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Period>> getActivePeriods() {
        return ResponseEntity.ok(timetableService.getActivePeriods());
    }

    @PutMapping("/periods/{id}")
    @Operation(summary = "Update period", description = "Update a period slot")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Period> updatePeriod(@PathVariable UUID id, @Valid @RequestBody Period period) {
        return ResponseEntity.ok(timetableService.updatePeriod(id, period));
    }

    @DeleteMapping("/periods/{id}")
    @Operation(summary = "Delete period", description = "Delete a period slot")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deletePeriod(@PathVariable UUID id) {
        timetableService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Timetable Entry Endpoints =====

    @PostMapping("/entries")
    @Operation(summary = "Create timetable entry", description = "Add a new timetable entry with conflict checking")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TimetableEntry> createEntry(@Valid @RequestBody TimetableEntry entry) {
        log.info("Creating timetable entry for class {} on {}", entry.getClassId(), entry.getDayOfWeek());
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.createEntry(entry));
    }

    @PostMapping("/entries/bulk")
    @Operation(summary = "Create bulk entries", description = "Add multiple timetable entries at once")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TimetableEntry>> createBulkEntries(
            @Valid @RequestBody List<TimetableEntry> entries) {
        log.info("Creating {} timetable entries", entries.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.createBulkEntries(entries));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get class timetable", description = "Get full timetable for a class")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TimetableEntry>> getClassTimetable(@PathVariable UUID classId) {
        return ResponseEntity.ok(timetableService.getClassTimetable(classId));
    }

    @GetMapping("/class/{classId}/section/{section}")
    @Operation(summary = "Get class section timetable", description = "Get timetable for a class section")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TimetableEntry>> getClassSectionTimetable(
            @PathVariable UUID classId, @PathVariable String section) {
        return ResponseEntity.ok(timetableService.getClassTimetableBySection(classId, section));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get teacher timetable", description = "Get full timetable for a teacher")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TimetableEntry>> getTeacherTimetable(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(timetableService.getTeacherTimetable(teacherId));
    }

    @GetMapping("/class/{classId}/day/{day}")
    @Operation(summary = "Get class day timetable", description = "Get timetable for a class on a specific day")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TimetableEntry>> getClassDayTimetable(
            @PathVariable UUID classId, @PathVariable TimetableEntry.DayOfWeek day) {
        return ResponseEntity.ok(timetableService.getClassDayTimetable(classId, day));
    }

    @GetMapping("/class/{classId}/weekly")
    @Operation(summary = "Get class weekly timetable", description = "Get weekly timetable grouped by day")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, List<TimetableEntry>>> getClassWeeklyTimetable(
            @PathVariable UUID classId) {
        return ResponseEntity.ok(timetableService.getClassWeeklyTimetable(classId));
    }

    @GetMapping("/teacher/{teacherId}/weekly")
    @Operation(summary = "Get teacher weekly timetable", description = "Get teacher's weekly timetable grouped by day")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, List<TimetableEntry>>> getTeacherWeeklyTimetable(
            @PathVariable UUID teacherId) {
        return ResponseEntity.ok(timetableService.getTeacherWeeklyTimetable(teacherId));
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Update timetable entry", description = "Update a timetable entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TimetableEntry> updateEntry(
            @PathVariable UUID id, @Valid @RequestBody TimetableEntry entry) {
        return ResponseEntity.ok(timetableService.updateEntry(id, entry));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete timetable entry", description = "Delete a timetable entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID id) {
        timetableService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/class/{classId}")
    @Operation(summary = "Delete class timetable", description = "Delete entire timetable for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteClassTimetable(@PathVariable UUID classId) {
        timetableService.deleteClassTimetable(classId);
        return ResponseEntity.noContent().build();
    }
}
