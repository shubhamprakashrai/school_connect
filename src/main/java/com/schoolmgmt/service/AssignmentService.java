package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateAssignmentRequest;
import com.schoolmgmt.dto.request.GradeSubmissionRequest;
import com.schoolmgmt.dto.request.SubmitAssignmentRequest;
import com.schoolmgmt.dto.request.UpdateAssignmentRequest;
import com.schoolmgmt.dto.response.AssignmentResponse;
import com.schoolmgmt.dto.response.AssignmentSubmissionResponse;
import com.schoolmgmt.model.Assignment;
import com.schoolmgmt.model.AssignmentSubmission;
import com.schoolmgmt.repository.AssignmentRepository;
import com.schoolmgmt.repository.AssignmentSubmissionRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    // ===== Assignment Operations =====

    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Assignment assignment = Assignment.builder()
                .tenantId(tenantId)
                .title(request.getTitle())
                .description(request.getDescription())
                .subjectId(request.getSubjectId() != null ? UUID.fromString(request.getSubjectId()) : null)
                .classId(UUID.fromString(request.getClassId()))
                .sectionId(request.getSectionId() != null ? UUID.fromString(request.getSectionId()) : null)
                .teacherId(UUID.fromString(request.getTeacherId()))
                .dueDate(request.getDueDate())
                .assignedDate(request.getAssignedDate() != null ? request.getAssignedDate() : LocalDate.now())
                .maxMarks(request.getMaxMarks() != null ? request.getMaxMarks() : 100)
                .attachmentUrl(request.getAttachmentUrl())
                .status(request.getStatus() != null
                        ? Assignment.AssignmentStatus.valueOf(request.getStatus())
                        : Assignment.AssignmentStatus.DRAFT)
                .type(request.getType() != null
                        ? Assignment.AssignmentType.valueOf(request.getType())
                        : Assignment.AssignmentType.HOMEWORK)
                .build();

        log.info("Creating assignment: {} for tenant: {}", assignment.getTitle(), tenantId);
        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse updateAssignment(UUID assignmentId, UpdateAssignmentRequest request) {
        Assignment existing = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found: " + assignmentId));

        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getSubjectId() != null) existing.setSubjectId(UUID.fromString(request.getSubjectId()));
        if (request.getClassId() != null) existing.setClassId(UUID.fromString(request.getClassId()));
        if (request.getSectionId() != null) existing.setSectionId(UUID.fromString(request.getSectionId()));
        if (request.getDueDate() != null) existing.setDueDate(request.getDueDate());
        if (request.getMaxMarks() != null) existing.setMaxMarks(request.getMaxMarks());
        if (request.getAttachmentUrl() != null) existing.setAttachmentUrl(request.getAttachmentUrl());
        if (request.getStatus() != null) existing.setStatus(Assignment.AssignmentStatus.valueOf(request.getStatus()));
        if (request.getType() != null) existing.setType(Assignment.AssignmentType.valueOf(request.getType()));

        Assignment saved = assignmentRepository.save(existing);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAssignment(UUID assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found: " + assignmentId));
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAssignments(UUID classId, UUID teacherId, UUID subjectId,
                                                    Assignment.AssignmentStatus status, Assignment.AssignmentType type,
                                                    Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        Page<Assignment> page = assignmentRepository.findByFilters(
                tenantId, classId, teacherId, subjectId, status, type, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByClass(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        return assignmentRepository.findByClassIdAndTenantId(classId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByTeacher(UUID teacherId) {
        String tenantId = TenantContext.getCurrentTenant();
        return assignmentRepository.findByTeacherIdAndTenantId(teacherId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsBySubject(UUID subjectId) {
        String tenantId = TenantContext.getCurrentTenant();
        return assignmentRepository.findBySubjectIdAndTenantId(subjectId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    // ===== Submission Operations =====

    @Transactional
    public AssignmentSubmissionResponse submitAssignment(UUID assignmentId, SubmitAssignmentRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        // Check if assignment exists
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found: " + assignmentId));

        UUID studentId = UUID.fromString(request.getStudentId());

        // Check if already submitted
        Optional<AssignmentSubmission> existingSubmission =
                submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);

        if (existingSubmission.isPresent()) {
            AssignmentSubmission existing = existingSubmission.get();
            existing.setContent(request.getContent());
            existing.setAttachmentUrl(request.getAttachmentUrl());
            existing.setSubmissionDate(LocalDateTime.now());
            existing.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);
            AssignmentSubmission saved = submissionRepository.save(existing);
            return toSubmissionResponse(saved);
        }

        // Determine if late
        boolean isLate = assignment.getDueDate() != null && LocalDate.now().isAfter(assignment.getDueDate());

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .tenantId(tenantId)
                .assignmentId(assignmentId)
                .studentId(studentId)
                .submissionDate(LocalDateTime.now())
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .status(isLate ? AssignmentSubmission.SubmissionStatus.LATE
                        : AssignmentSubmission.SubmissionStatus.SUBMITTED)
                .build();

        log.info("Student {} submitting assignment {}", studentId, assignmentId);
        AssignmentSubmission saved = submissionRepository.save(submission);
        return toSubmissionResponse(saved);
    }

    @Transactional
    public AssignmentSubmissionResponse gradeSubmission(UUID submissionId, GradeSubmissionRequest request) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        submission.setMarksObtained(request.getMarksObtained());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.GRADED);

        log.info("Grading submission {} with marks {}", submissionId, request.getMarksObtained());
        AssignmentSubmission saved = submissionRepository.save(submission);
        return toSubmissionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getSubmissionsByAssignment(UUID assignmentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return submissionRepository.findByAssignmentIdAndTenantId(assignmentId, tenantId)
                .stream().map(this::toSubmissionResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getSubmissionsByStudent(UUID studentId) {
        String tenantId = TenantContext.getCurrentTenant();
        return submissionRepository.findByStudentIdAndTenantId(studentId, tenantId)
                .stream().map(this::toSubmissionResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssignmentStatistics(UUID assignmentId) {
        String tenantId = TenantContext.getCurrentTenant();

        Long totalSubmissions = submissionRepository.countByAssignmentIdAndTenantId(assignmentId, tenantId);
        Long submitted = submissionRepository.countSubmittedByAssignmentIdAndTenantId(assignmentId, tenantId);
        Long graded = submissionRepository.countGradedByAssignmentIdAndTenantId(assignmentId, tenantId);
        Double averageMarks = submissionRepository.getAverageMarksByAssignmentId(assignmentId, tenantId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("submittedCount", submitted);
        stats.put("gradedCount", graded);
        stats.put("pendingCount", totalSubmissions - submitted - graded);
        stats.put("averageMarks", averageMarks != null ? averageMarks : 0.0);

        return stats;
    }

    // ===== Mappers =====

    private AssignmentResponse toResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId().toString())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .subjectId(assignment.getSubjectId() != null ? assignment.getSubjectId().toString() : null)
                .classId(assignment.getClassId().toString())
                .sectionId(assignment.getSectionId() != null ? assignment.getSectionId().toString() : null)
                .teacherId(assignment.getTeacherId().toString())
                .dueDate(assignment.getDueDate())
                .assignedDate(assignment.getAssignedDate())
                .maxMarks(assignment.getMaxMarks())
                .attachmentUrl(assignment.getAttachmentUrl())
                .status(assignment.getStatus().name())
                .type(assignment.getType().name())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private AssignmentSubmissionResponse toSubmissionResponse(AssignmentSubmission submission) {
        return AssignmentSubmissionResponse.builder()
                .id(submission.getId().toString())
                .assignmentId(submission.getAssignmentId().toString())
                .studentId(submission.getStudentId().toString())
                .submissionDate(submission.getSubmissionDate())
                .content(submission.getContent())
                .attachmentUrl(submission.getAttachmentUrl())
                .marksObtained(submission.getMarksObtained())
                .feedback(submission.getFeedback())
                .status(submission.getStatus().name())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
