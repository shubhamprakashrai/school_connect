package com.schoolmgmt.repository;

import com.schoolmgmt.model.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    Page<Exam> findByTenantId(String tenantId, Pageable pageable);

    List<Exam> findByTenantIdAndClassId(String tenantId, UUID classId);

    List<Exam> findByTenantIdAndClassIdAndExamDateBetween(
            String tenantId, UUID classId, LocalDate startDate, LocalDate endDate);

    Page<Exam> findByTenantIdAndExamTypeId(String tenantId, UUID examTypeId, Pageable pageable);

    @Query("SELECT e FROM Exam e WHERE e.tenantId = :tenantId AND e.examDate >= :date ORDER BY e.examDate ASC")
    List<Exam> findUpcomingExams(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT e FROM Exam e WHERE e.tenantId = :tenantId AND e.classId = :classId AND e.examDate >= :date ORDER BY e.examDate ASC")
    List<Exam> findUpcomingExamsByClass(
            @Param("tenantId") String tenantId,
            @Param("classId") UUID classId,
            @Param("date") LocalDate date);

    List<Exam> findByTenantIdAndStatus(String tenantId, Exam.ExamStatus status);
}
