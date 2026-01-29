package com.schoolmgmt.controller;

import com.schoolmgmt.model.Exam;
import com.schoolmgmt.model.ExamResult;
import com.schoolmgmt.model.ExamType;
import com.schoolmgmt.service.ExamService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exam Management", description = "APIs for managing exams, results, and report cards")
public class ExamController {

    private final ExamService examService;

    // ===== Exam Type Endpoints =====

    @PostMapping("/types")
    @Operation(summary = "Create exam type", description = "Create a new exam type (e.g., Unit Test, Mid-Term)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ExamType> createExamType(@Valid @RequestBody ExamType examType) {
        log.info("Creating exam type: {}", examType.getName());
        ExamType created = examService.createExamType(examType);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/types")
    @Operation(summary = "Get exam types", description = "Get all exam types for the tenant")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ExamType>> getExamTypes() {
        return ResponseEntity.ok(examService.getExamTypes());
    }

    @GetMapping("/types/active")
    @Operation(summary = "Get active exam types", description = "Get only active exam types")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ExamType>> getActiveExamTypes() {
        return ResponseEntity.ok(examService.getActiveExamTypes());
    }

    @PutMapping("/types/{id}")
    @Operation(summary = "Update exam type", description = "Update an exam type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ExamType> updateExamType(@PathVariable UUID id, @Valid @RequestBody ExamType examType) {
        return ResponseEntity.ok(examService.updateExamType(id, examType));
    }

    @DeleteMapping("/types/{id}")
    @Operation(summary = "Delete exam type", description = "Delete an exam type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteExamType(@PathVariable UUID id) {
        examService.deleteExamType(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Exam Endpoints =====

    @PostMapping
    @Operation(summary = "Create exam", description = "Schedule a new exam")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Exam> createExam(@Valid @RequestBody Exam exam) {
        log.info("Creating exam: {}", exam.getName());
        Exam created = examService.createExam(exam);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all exams", description = "Get paginated list of exams")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Page<Exam>> getExams(
            @PageableDefault(size = 20, sort = "examDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(examService.getExams(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exam by ID", description = "Get exam details")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Exam> getExamById(@PathVariable UUID id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get exams by class", description = "Get all exams for a class")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Exam>> getExamsByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(examService.getExamsByClass(classId));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming exams", description = "Get upcoming exams for the tenant")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Exam>> getUpcomingExams() {
        return ResponseEntity.ok(examService.getUpcomingExams());
    }

    @GetMapping("/upcoming/class/{classId}")
    @Operation(summary = "Get upcoming exams by class", description = "Get upcoming exams for a specific class")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Exam>> getUpcomingExamsByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(examService.getUpcomingExamsByClass(classId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exam", description = "Update exam details")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Exam> updateExam(@PathVariable UUID id, @Valid @RequestBody Exam exam) {
        return ResponseEntity.ok(examService.updateExam(id, exam));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exam", description = "Delete an exam")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable UUID id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Exam Result Endpoints =====

    @PostMapping("/{examId}/marks")
    @Operation(summary = "Enter marks", description = "Enter marks for a student in an exam")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ExamResult> enterMarks(
            @PathVariable UUID examId,
            @Valid @RequestBody ExamResult result) {
        Exam exam = examService.getExamById(examId);
        result.setExam(exam);
        log.info("Entering marks for student: {} in exam: {}", result.getStudentId(), examId);
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.enterMarks(result));
    }

    @PostMapping("/{examId}/marks/bulk")
    @Operation(summary = "Enter bulk marks", description = "Enter marks for multiple students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ExamResult>> enterBulkMarks(
            @PathVariable UUID examId,
            @Valid @RequestBody List<ExamResult> results) {
        Exam exam = examService.getExamById(examId);
        results.forEach(r -> r.setExam(exam));
        log.info("Entering bulk marks for {} students in exam: {}", results.size(), examId);
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.enterBulkMarks(results));
    }

    @GetMapping("/{examId}/results")
    @Operation(summary = "Get exam results", description = "Get all results for an exam")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ExamResult>> getExamResults(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.getResultsByExam(examId));
    }

    @GetMapping("/{examId}/statistics")
    @Operation(summary = "Get exam statistics", description = "Get statistics for an exam (avg, pass rate, etc.)")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getExamStatistics(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.getExamStatistics(examId));
    }

    @GetMapping("/students/{studentId}/results")
    @Operation(summary = "Get student results", description = "Get all results for a student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ExamResult>> getStudentResults(@PathVariable UUID studentId) {
        return ResponseEntity.ok(examService.getStudentResults(studentId));
    }

    @GetMapping("/students/{studentId}/report-card")
    @Operation(summary = "Generate report card", description = "Generate a report card for a student")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Object>> getReportCard(@PathVariable UUID studentId) {
        return ResponseEntity.ok(examService.generateReportCard(studentId));
    }
}
