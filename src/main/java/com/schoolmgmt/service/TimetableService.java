package com.schoolmgmt.service;

import com.schoolmgmt.model.Period;
import com.schoolmgmt.model.TimetableEntry;
import com.schoolmgmt.repository.PeriodRepository;
import com.schoolmgmt.repository.TimetableRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableService {

    private final PeriodRepository periodRepository;
    private final TimetableRepository timetableRepository;

    // ===== Period Operations =====

    @Transactional
    public Period createPeriod(Period period) {
        String tenantId = TenantContext.getCurrentTenant();
        period.setTenantId(tenantId);

        if (periodRepository.existsByTenantIdAndPeriodNumber(tenantId, period.getPeriodNumber())) {
            throw new IllegalArgumentException(
                    "Period number " + period.getPeriodNumber() + " already exists");
        }

        log.info("Creating period {} for tenant {}", period.getName(), tenantId);
        return periodRepository.save(period);
    }

    @Transactional(readOnly = true)
    public List<Period> getAllPeriods() {
        String tenantId = TenantContext.getCurrentTenant();
        return periodRepository.findByTenantIdOrderByPeriodNumber(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Period> getActivePeriods() {
        String tenantId = TenantContext.getCurrentTenant();
        return periodRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public Period updatePeriod(UUID periodId, Period updated) {
        Period existing = periodRepository.findById(periodId)
                .orElseThrow(() -> new NoSuchElementException("Period not found: " + periodId));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getStartTime() != null) existing.setStartTime(updated.getStartTime());
        if (updated.getEndTime() != null) existing.setEndTime(updated.getEndTime());
        if (updated.getIsBreak() != null) existing.setIsBreak(updated.getIsBreak());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return periodRepository.save(existing);
    }

    @Transactional
    public void deletePeriod(UUID periodId) {
        periodRepository.deleteById(periodId);
    }

    // ===== Timetable Operations =====

    @Transactional
    public TimetableEntry createEntry(TimetableEntry entry) {
        String tenantId = TenantContext.getCurrentTenant();
        entry.setTenantId(tenantId);

        // Check for class conflicts
        List<TimetableEntry> classConflicts = timetableRepository.findConflictingClassEntries(
                tenantId, entry.getClassId(), entry.getDayOfWeek(), entry.getPeriod().getId());
        if (!classConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Class already has a timetable entry for this period on " + entry.getDayOfWeek());
        }

        // Check for teacher conflicts
        if (entry.getTeacherId() != null) {
            List<TimetableEntry> teacherConflicts = timetableRepository.findConflictingTeacherEntries(
                    tenantId, entry.getTeacherId(), entry.getDayOfWeek(), entry.getPeriod().getId());
            if (!teacherConflicts.isEmpty()) {
                throw new IllegalArgumentException(
                        "Teacher is already assigned to another class for this period on " + entry.getDayOfWeek());
            }
        }

        log.info("Creating timetable entry for class {} on {} period {}",
                entry.getClassId(), entry.getDayOfWeek(), entry.getPeriod().getPeriodNumber());
        return timetableRepository.save(entry);
    }

    @Transactional
    public List<TimetableEntry> createBulkEntries(List<TimetableEntry> entries) {
        String tenantId = TenantContext.getCurrentTenant();
        entries.forEach(e -> e.setTenantId(tenantId));
        log.info("Creating {} timetable entries", entries.size());
        return timetableRepository.saveAll(entries);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getClassTimetable(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        return timetableRepository.findByTenantIdAndClassIdAndIsActiveTrue(tenantId, classId);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getClassTimetableBySection(UUID classId, String section) {
        String tenantId = TenantContext.getCurrentTenant();
        return timetableRepository.findByTenantIdAndClassIdAndSectionAndIsActiveTrue(
                tenantId, classId, section);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getTeacherTimetable(UUID teacherId) {
        String tenantId = TenantContext.getCurrentTenant();
        return timetableRepository.findByTenantIdAndTeacherIdAndIsActiveTrue(tenantId, teacherId);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getClassDayTimetable(UUID classId, TimetableEntry.DayOfWeek day) {
        String tenantId = TenantContext.getCurrentTenant();
        return timetableRepository.findByTenantIdAndClassIdAndDayOfWeekAndIsActiveTrue(
                tenantId, classId, day);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getTeacherDayTimetable(UUID teacherId, TimetableEntry.DayOfWeek day) {
        String tenantId = TenantContext.getCurrentTenant();
        return timetableRepository.findByTenantIdAndTeacherIdAndDayOfWeekAndIsActiveTrue(
                tenantId, teacherId, day);
    }

    @Transactional(readOnly = true)
    public Map<String, List<TimetableEntry>> getClassWeeklyTimetable(UUID classId) {
        List<TimetableEntry> entries = getClassTimetable(classId);
        return entries.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDayOfWeek().name(),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Map<String, List<TimetableEntry>> getTeacherWeeklyTimetable(UUID teacherId) {
        List<TimetableEntry> entries = getTeacherTimetable(teacherId);
        return entries.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDayOfWeek().name(),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    @Transactional
    public TimetableEntry updateEntry(UUID entryId, TimetableEntry updated) {
        TimetableEntry existing = timetableRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException("Timetable entry not found: " + entryId));

        if (updated.getSubjectName() != null) existing.setSubjectName(updated.getSubjectName());
        if (updated.getSubjectId() != null) existing.setSubjectId(updated.getSubjectId());
        if (updated.getTeacherId() != null) existing.setTeacherId(updated.getTeacherId());
        if (updated.getTeacherName() != null) existing.setTeacherName(updated.getTeacherName());
        if (updated.getRoom() != null) existing.setRoom(updated.getRoom());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return timetableRepository.save(existing);
    }

    @Transactional
    public void deleteEntry(UUID entryId) {
        timetableRepository.deleteById(entryId);
    }

    @Transactional
    public void deleteClassTimetable(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        timetableRepository.deleteByTenantIdAndClassId(tenantId, classId);
    }
}
