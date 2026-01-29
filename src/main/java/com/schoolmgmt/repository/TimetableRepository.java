package com.schoolmgmt.repository;

import com.schoolmgmt.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableEntry, UUID> {

    List<TimetableEntry> findByTenantIdAndClassIdAndIsActiveTrue(
            String tenantId, UUID classId);

    List<TimetableEntry> findByTenantIdAndClassIdAndSectionAndIsActiveTrue(
            String tenantId, UUID classId, String section);

    List<TimetableEntry> findByTenantIdAndTeacherIdAndIsActiveTrue(
            String tenantId, UUID teacherId);

    List<TimetableEntry> findByTenantIdAndClassIdAndDayOfWeekAndIsActiveTrue(
            String tenantId, UUID classId, TimetableEntry.DayOfWeek dayOfWeek);

    List<TimetableEntry> findByTenantIdAndTeacherIdAndDayOfWeekAndIsActiveTrue(
            String tenantId, UUID teacherId, TimetableEntry.DayOfWeek dayOfWeek);

    @Query("SELECT t FROM TimetableEntry t WHERE t.tenantId = :tenantId " +
           "AND t.classId = :classId AND t.dayOfWeek = :day AND t.period.id = :periodId " +
           "AND t.isActive = true")
    List<TimetableEntry> findConflictingClassEntries(
            @Param("tenantId") String tenantId,
            @Param("classId") UUID classId,
            @Param("day") TimetableEntry.DayOfWeek day,
            @Param("periodId") UUID periodId);

    @Query("SELECT t FROM TimetableEntry t WHERE t.tenantId = :tenantId " +
           "AND t.teacherId = :teacherId AND t.dayOfWeek = :day AND t.period.id = :periodId " +
           "AND t.isActive = true")
    List<TimetableEntry> findConflictingTeacherEntries(
            @Param("tenantId") String tenantId,
            @Param("teacherId") UUID teacherId,
            @Param("day") TimetableEntry.DayOfWeek day,
            @Param("periodId") UUID periodId);

    void deleteByTenantIdAndClassId(String tenantId, UUID classId);
}
