package com.schoolmgmt.repository;

import com.schoolmgmt.model.ExamResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

    List<ExamResult> findByExamId(UUID examId);

    Page<ExamResult> findByExamId(UUID examId, Pageable pageable);

    List<ExamResult> findByStudentId(UUID studentId);

    Optional<ExamResult> findByExamIdAndStudentId(UUID examId, UUID studentId);

    boolean existsByExamIdAndStudentId(UUID examId, UUID studentId);

    @Query("SELECT er FROM ExamResult er JOIN er.exam e WHERE e.tenantId = :tenantId AND er.studentId = :studentId ORDER BY e.examDate DESC")
    List<ExamResult> findByTenantIdAndStudentId(
            @Param("tenantId") String tenantId,
            @Param("studentId") UUID studentId);

    @Query("SELECT er FROM ExamResult er WHERE er.exam.id = :examId ORDER BY er.marksObtained DESC")
    List<ExamResult> findByExamIdOrderByMarksDesc(@Param("examId") UUID examId);

    @Query("SELECT AVG(er.percentage) FROM ExamResult er WHERE er.exam.id = :examId AND er.isAbsent = false")
    Double getAveragePercentageByExamId(@Param("examId") UUID examId);

    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.exam.id = :examId AND er.resultStatus = 'PASS'")
    Long countPassedByExamId(@Param("examId") UUID examId);

    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.exam.id = :examId AND er.resultStatus = 'FAIL'")
    Long countFailedByExamId(@Param("examId") UUID examId);
}
