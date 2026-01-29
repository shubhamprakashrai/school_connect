package com.schoolmgmt.service;

import com.schoolmgmt.model.Exam;
import com.schoolmgmt.model.ExamResult;
import com.schoolmgmt.model.ExamType;
import com.schoolmgmt.repository.ExamRepository;
import com.schoolmgmt.repository.ExamResultRepository;
import com.schoolmgmt.repository.ExamTypeRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamResultRepository resultRepository;
    private final ExamTypeRepository examTypeRepository;

    // ===== Exam Type Operations =====

    @Transactional
    public ExamType createExamType(ExamType examType) {
        String tenantId = TenantContext.getCurrentTenant();
        examType.setTenantId(tenantId);

        if (examTypeRepository.existsByTenantIdAndName(tenantId, examType.getName())) {
            throw new IllegalArgumentException("Exam type with name '" + examType.getName() + "' already exists");
        }

        log.info("Creating exam type: {} for tenant: {}", examType.getName(), tenantId);
        return examTypeRepository.save(examType);
    }

    @Transactional(readOnly = true)
    public List<ExamType> getExamTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return examTypeRepository.findByTenantIdOrderByDisplayOrder(tenantId);
    }

    @Transactional(readOnly = true)
    public List<ExamType> getActiveExamTypes() {
        String tenantId = TenantContext.getCurrentTenant();
        return examTypeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public ExamType updateExamType(UUID examTypeId, ExamType updated) {
        ExamType existing = examTypeRepository.findById(examTypeId)
                .orElseThrow(() -> new NoSuchElementException("Exam type not found: " + examTypeId));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getWeightage() != null) existing.setWeightage(updated.getWeightage());
        if (updated.getMaxMarks() != null) existing.setMaxMarks(updated.getMaxMarks());
        if (updated.getPassingMarks() != null) existing.setPassingMarks(updated.getPassingMarks());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());
        if (updated.getDisplayOrder() != null) existing.setDisplayOrder(updated.getDisplayOrder());

        return examTypeRepository.save(existing);
    }

    @Transactional
    public void deleteExamType(UUID examTypeId) {
        examTypeRepository.deleteById(examTypeId);
    }

    // ===== Exam Operations =====

    @Transactional
    public Exam createExam(Exam exam) {
        String tenantId = TenantContext.getCurrentTenant();
        exam.setTenantId(tenantId);
        log.info("Creating exam: {} for tenant: {}", exam.getName(), tenantId);
        return examRepository.save(exam);
    }

    @Transactional(readOnly = true)
    public Page<Exam> getExams(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return examRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Exam getExamById(UUID examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));
    }

    @Transactional(readOnly = true)
    public List<Exam> getExamsByClass(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        return examRepository.findByTenantIdAndClassId(tenantId, classId);
    }

    @Transactional(readOnly = true)
    public List<Exam> getUpcomingExams() {
        String tenantId = TenantContext.getCurrentTenant();
        return examRepository.findUpcomingExams(tenantId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Exam> getUpcomingExamsByClass(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        return examRepository.findUpcomingExamsByClass(tenantId, classId, LocalDate.now());
    }

    @Transactional
    public Exam updateExam(UUID examId, Exam updated) {
        Exam existing = getExamById(examId);

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getExamDate() != null) existing.setExamDate(updated.getExamDate());
        if (updated.getStartTime() != null) existing.setStartTime(updated.getStartTime());
        if (updated.getEndTime() != null) existing.setEndTime(updated.getEndTime());
        if (updated.getMaxMarks() != null) existing.setMaxMarks(updated.getMaxMarks());
        if (updated.getPassingMarks() != null) existing.setPassingMarks(updated.getPassingMarks());
        if (updated.getRoom() != null) existing.setRoom(updated.getRoom());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getInstructions() != null) existing.setInstructions(updated.getInstructions());

        return examRepository.save(existing);
    }

    @Transactional
    public void deleteExam(UUID examId) {
        examRepository.deleteById(examId);
    }

    // ===== Exam Result Operations =====

    @Transactional
    public ExamResult enterMarks(ExamResult result) {
        String tenantId = TenantContext.getCurrentTenant();
        result.setTenantId(tenantId);

        // Check if result already exists
        Optional<ExamResult> existingResult = resultRepository
                .findByExamIdAndStudentId(result.getExam().getId(), result.getStudentId());

        if (existingResult.isPresent()) {
            ExamResult existing = existingResult.get();
            existing.setMarksObtained(result.getMarksObtained());
            existing.setMaxMarks(result.getMaxMarks());
            existing.setIsAbsent(result.getIsAbsent());
            existing.setRemarks(result.getRemarks());
            return resultRepository.save(existing);
        }

        log.info("Entering marks for student: {} in exam: {}",
                result.getStudentId(), result.getExam().getId());
        return resultRepository.save(result);
    }

    @Transactional
    public List<ExamResult> enterBulkMarks(List<ExamResult> results) {
        String tenantId = TenantContext.getCurrentTenant();
        results.forEach(r -> r.setTenantId(tenantId));
        log.info("Entering bulk marks for {} students", results.size());
        return resultRepository.saveAll(results);
    }

    @Transactional(readOnly = true)
    public List<ExamResult> getResultsByExam(UUID examId) {
        return resultRepository.findByExamIdOrderByMarksDesc(examId);
    }

    @Transactional(readOnly = true)
    public Page<ExamResult> getResultsByExamPaginated(UUID examId, Pageable pageable) {
        return resultRepository.findByExamId(examId, pageable);
    }

    @Transactional(readOnly = true)
    public List<ExamResult> getStudentResults(UUID studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return resultRepository.findByTenantIdAndStudentId(tenantId, studentId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExamStatistics(UUID examId) {
        List<ExamResult> results = resultRepository.findByExamIdOrderByMarksDesc(examId);
        Double avgPercentage = resultRepository.getAveragePercentageByExamId(examId);
        Long passed = resultRepository.countPassedByExamId(examId);
        Long failed = resultRepository.countFailedByExamId(examId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", results.size());
        stats.put("averagePercentage", avgPercentage != null ? avgPercentage : 0.0);
        stats.put("passed", passed);
        stats.put("failed", failed);
        stats.put("passPercentage", results.isEmpty() ? 0.0
                : (passed.doubleValue() / results.size()) * 100);

        if (!results.isEmpty()) {
            stats.put("highestMarks", results.get(0).getMarksObtained());
            stats.put("lowestMarks", results.get(results.size() - 1).getMarksObtained());
            stats.put("topper", results.get(0).getStudentName());
        }

        return stats;
    }

    /**
     * Generate report card data for a student
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateReportCard(UUID studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        List<ExamResult> results = resultRepository.findByTenantIdAndStudentId(tenantId, studentId);

        Map<String, Object> reportCard = new HashMap<>();
        reportCard.put("studentId", studentId);
        reportCard.put("results", results);

        // Calculate overall percentage
        double totalMarks = 0;
        double totalMaxMarks = 0;
        for (ExamResult result : results) {
            if (!result.getIsAbsent()) {
                totalMarks += result.getMarksObtained();
                totalMaxMarks += result.getMaxMarks();
            }
        }

        double overallPercentage = totalMaxMarks > 0 ? (totalMarks / totalMaxMarks) * 100 : 0;
        reportCard.put("overallPercentage", overallPercentage);
        reportCard.put("overallGrade", getOverallGrade(overallPercentage));
        reportCard.put("totalExams", results.size());
        reportCard.put("examsTaken", results.stream().filter(r -> !r.getIsAbsent()).count());

        return reportCard;
    }

    private String getOverallGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        if (percentage >= 33) return "E";
        return "F";
    }
}
