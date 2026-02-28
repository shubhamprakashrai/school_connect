package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentUpdateRequest;
import com.schoolmgmt.dto.request.TeacherBatchAssignmentRequest;
import com.schoolmgmt.dto.response.TeacherAssignmentResponse;
import com.schoolmgmt.dto.response.TeacherSubjectInfoResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.model.Subject;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.model.TeacherClass;
import com.schoolmgmt.repository.AcademicYearRepository;
import com.schoolmgmt.repository.SectionRepository;
import com.schoolmgmt.repository.SubjectRepository;
import com.schoolmgmt.repository.TeacherClassRepository;
import com.schoolmgmt.repository.TeacherRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing teacher assignments to sections and subjects.
 * Handles assignment of teachers as subject teachers to specific sections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {

    private final TeacherClassRepository teacherClassRepository;
    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;

    /**
     * Creates a new teacher assignment to a section and subject.
     *
     * @param request the assignment request
     * @return the created assignment response
     * @throws ResourceNotFoundException if teacher, section, subject, or academic year not found
     * @throws BusinessException if assignment already exists
     */
    @Transactional
    public TeacherAssignmentResponse createAssignment(TeacherAssignmentRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Creating teacher assignment for tenant: {}, teacher: {}, section: {}, subject: {}",
                    tenantId, request.getTeacherId(), request.getSectionId(), request.getSubjectId());

            // Validate teacher exists and is active
            Teacher teacher = teacherRepository.findByIdAndTenantId(request.getTeacherId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

            if (!teacher.isActive()) {
                throw new BusinessException("Cannot assign inactive teacher to a section");
            }

            // Validate section exists
            Section section = sectionRepository.findByIdAndTenantId(request.getSectionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

            // Validate subject exists
            Subject subject = subjectRepository.findByIdAndTenantId(request.getSubjectId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

            // Validate academic year exists
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

            // Check for duplicate assignment
            if (teacherClassRepository.existsByTeacherIdAndSectionIdAndSubjectIdAndAcademicYearId(
                    request.getTeacherId(), request.getSectionId(), request.getSubjectId(), request.getAcademicYearId())) {
                throw new BusinessException("Teacher is already assigned to this section and subject for the academic year");
            }

            // Create assignment
            TeacherClass assignment = TeacherClass.builder()
                    .teacherId(request.getTeacherId())
                    .sectionId(request.getSectionId())
                    .subjectId(request.getSubjectId())
                    .academicYearId(request.getAcademicYearId())
                    .isActive(true)
                    .assignedDate(LocalDate.now())
                    .build();

            TeacherClass savedAssignment = teacherClassRepository.save(assignment);
            log.info("Teacher assignment created successfully: {} for teacher {} to section {} subject {}",
                    savedAssignment.getId(), teacher.getFullName(), section.getName(), subject.getName());

            return toTeacherAssignmentResponse(savedAssignment, teacher, section, subject, academicYear);

        } catch (ResourceNotFoundException | BusinessException e) {
            log.warn("Business exception while creating teacher assignment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating teacher assignment: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating teacher assignment", e);
        }
    }

    /**
     * Gets a teacher assignment by ID.
     *
     * @param assignmentId the assignment ID
     * @return the assignment response
     * @throws ResourceNotFoundException if assignment not found
     */
    @Transactional(readOnly = true)
    public TeacherAssignmentResponse getAssignmentById(UUID assignmentId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching teacher assignment: {} for tenant: {}", assignmentId, tenantId);

            TeacherClass assignment = teacherClassRepository.findByIdAndTenantId(assignmentId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("TeacherAssignment", "id", assignmentId));

            Teacher teacher = teacherRepository.findById(assignment.getTeacherId()).orElse(null);
            Section section = assignment.getSectionId() != null ?
                    sectionRepository.findById(assignment.getSectionId()).orElse(null) : null;
            Subject subject = assignment.getSubjectId() != null ?
                    subjectRepository.findById(assignment.getSubjectId()).orElse(null) : null;
            AcademicYear academicYear = academicYearRepository.findById(assignment.getAcademicYearId()).orElse(null);

            return toTeacherAssignmentResponse(assignment, teacher, section, subject, academicYear);

        } catch (ResourceNotFoundException e) {
            log.warn("Teacher assignment not found: {}", assignmentId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching teacher assignment {}: {}", assignmentId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching teacher assignment", e);
        }
    }

    /**
     * Gets all assignments for a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of assignment responses
     */
    @Transactional(readOnly = true)
    public List<TeacherAssignmentResponse> getAssignmentsByTeacher(UUID teacherId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching assignments for teacher: {} in tenant: {}", teacherId, tenantId);

            // Verify teacher exists
            teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            List<TeacherClass> assignments = teacherClassRepository.findByTeacherIdAndTenantId(teacherId, tenantId);

            return assignments.stream()
                    .map(assignment -> {
                        Teacher teacher = teacherRepository.findById(assignment.getTeacherId()).orElse(null);
                        Section section = assignment.getSectionId() != null ?
                                sectionRepository.findById(assignment.getSectionId()).orElse(null) : null;
                        Subject subject = assignment.getSubjectId() != null ?
                                subjectRepository.findById(assignment.getSubjectId()).orElse(null) : null;
                        AcademicYear academicYear = academicYearRepository.findById(assignment.getAcademicYearId()).orElse(null);
                        return toTeacherAssignmentResponse(assignment, teacher, section, subject, academicYear);
                    })
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            log.warn("Exception while fetching teacher assignments: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching assignments for teacher {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching teacher assignments", e);
        }
    }

    /**
     * Gets all assignments for a specific section.
     *
     * @param sectionId the section ID
     * @return list of assignment responses
     */
    @Transactional(readOnly = true)
    public List<TeacherAssignmentResponse> getAssignmentsBySection(UUID sectionId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching assignments for section: {} in tenant: {}", sectionId, tenantId);

            // Verify section exists
            sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

            List<TeacherClass> assignments = teacherClassRepository.findBySectionIdAndTenantId(sectionId, tenantId);

            return assignments.stream()
                    .map(assignment -> {
                        Teacher teacher = teacherRepository.findById(assignment.getTeacherId()).orElse(null);
                        Section section = assignment.getSectionId() != null ?
                                sectionRepository.findById(assignment.getSectionId()).orElse(null) : null;
                        Subject subject = assignment.getSubjectId() != null ?
                                subjectRepository.findById(assignment.getSubjectId()).orElse(null) : null;
                        AcademicYear academicYear = academicYearRepository.findById(assignment.getAcademicYearId()).orElse(null);
                        return toTeacherAssignmentResponse(assignment, teacher, section, subject, academicYear);
                    })
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            log.warn("Exception while fetching section assignments: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching assignments for section {}: {}", sectionId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching section assignments", e);
        }
    }

    /**
     * Updates a teacher assignment.
     *
     * @param assignmentId the assignment ID
     * @param request the update request
     * @return the updated assignment response
     */
    @Transactional
    public TeacherAssignmentResponse updateAssignment(UUID assignmentId, TeacherAssignmentUpdateRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Updating teacher assignment: {} for tenant: {}", assignmentId, tenantId);

            TeacherClass assignment = teacherClassRepository.findByIdAndTenantId(assignmentId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("TeacherAssignment", "id", assignmentId));

            if (request.getIsActive() != null) {
                if (request.getIsActive()) {
                    assignment.activate();
                } else {
                    assignment.deactivate();
                }
            }

            TeacherClass updatedAssignment = teacherClassRepository.save(assignment);
            log.info("Teacher assignment updated successfully: {}", assignmentId);

            Teacher teacher = teacherRepository.findById(assignment.getTeacherId()).orElse(null);
            Section section = assignment.getSectionId() != null ?
                    sectionRepository.findById(assignment.getSectionId()).orElse(null) : null;
            Subject subject = assignment.getSubjectId() != null ?
                    subjectRepository.findById(assignment.getSubjectId()).orElse(null) : null;
            AcademicYear academicYear = academicYearRepository.findById(assignment.getAcademicYearId()).orElse(null);

            return toTeacherAssignmentResponse(updatedAssignment, teacher, section, subject, academicYear);

        } catch (ResourceNotFoundException e) {
            log.warn("Teacher assignment not found for update: {}", assignmentId);
            throw e;
        } catch (Exception e) {
            log.error("Error updating teacher assignment {}: {}", assignmentId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while updating teacher assignment", e);
        }
    }

    /**
     * Deletes a teacher assignment.
     *
     * @param assignmentId the assignment ID
     */
    @Transactional
    public void deleteAssignment(UUID assignmentId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Deleting teacher assignment: {} for tenant: {}", assignmentId, tenantId);

            TeacherClass assignment = teacherClassRepository.findByIdAndTenantId(assignmentId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("TeacherAssignment", "id", assignmentId));

            teacherClassRepository.delete(assignment);
            log.info("Teacher assignment deleted successfully: {}", assignmentId);

        } catch (ResourceNotFoundException e) {
            log.warn("Teacher assignment not found for deletion: {}", assignmentId);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting teacher assignment {}: {}", assignmentId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while deleting teacher assignment", e);
        }
    }

    /**
     * Batch creates multiple subject assignments for a teacher in a section.
     *
     * @param request the batch assignment request
     * @return list of created assignment responses
     */
    @Transactional
    public List<TeacherAssignmentResponse> createBatchAssignments(TeacherBatchAssignmentRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Creating batch assignments for teacher: {}, section: {}, subjects: {}",
                    request.getTeacherId(), request.getSectionId(), request.getSubjectIds().size());

            // Validate teacher exists and is active
            Teacher teacher = teacherRepository.findByIdAndTenantId(request.getTeacherId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

            if (!teacher.isActive()) {
                throw new BusinessException("Cannot assign inactive teacher to subjects");
            }

            // Validate section exists
            Section section = sectionRepository.findByIdAndTenantId(request.getSectionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

            // Validate academic year exists
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

            List<TeacherAssignmentResponse> responses = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Process each subject
            for (UUID subjectId : request.getSubjectIds()) {
                try {
                    // Validate subject exists
                    Subject subject = subjectRepository.findByIdAndTenantId(subjectId, tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

                    // Check for duplicate assignment
                    if (teacherClassRepository.existsByTeacherIdAndSectionIdAndSubjectIdAndAcademicYearId(
                            request.getTeacherId(), request.getSectionId(), subjectId, request.getAcademicYearId())) {
                        errors.add("Subject " + subject.getName() + " is already assigned");
                        continue;
                    }

                    // Create assignment
                    TeacherClass assignment = TeacherClass.builder()
                            .teacherId(request.getTeacherId())
                            .sectionId(request.getSectionId())
                            .subjectId(subjectId)
                            .academicYearId(request.getAcademicYearId())
                            .isActive(true)
                            .assignedDate(LocalDate.now())
                            .build();

                    TeacherClass savedAssignment = teacherClassRepository.save(assignment);
                    responses.add(toTeacherAssignmentResponse(savedAssignment, teacher, section, subject, academicYear));

                } catch (ResourceNotFoundException e) {
                    errors.add("Subject not found: " + subjectId);
                }
            }

            if (!errors.isEmpty()) {
                log.warn("Batch assignment completed with errors: {}", errors);
            }

            log.info("Batch assignment completed: {} subjects assigned, {} errors",
                    responses.size(), errors.size());

            return responses;

        } catch (ResourceNotFoundException | BusinessException e) {
            log.warn("Business exception in batch assignment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in batch assignment: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating batch assignments", e);
        }
    }

    /**
     * Gets teacher subject information for profile - includes count of subjects taught.
     *
     * @param teacherId the teacher ID
     * @return teacher subject info response
     */
    @Transactional(readOnly = true)
    public TeacherSubjectInfoResponse getTeacherSubjectInfo(UUID teacherId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching subject info for teacher: {} in tenant: {}", teacherId, tenantId);

            // Verify teacher exists
            Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            // Get all active assignments for teacher
            List<TeacherClass> assignments = teacherClassRepository.findByTeacherIdAndIsActiveTrueAndTenantId(teacherId, tenantId);

            // Get unique subjects count
            long uniqueSubjectsCount = teacherClassRepository.countDistinctSubjectsByTeacherId(teacherId, tenantId);
            List<UUID> subjectIds = teacherClassRepository.findDistinctSubjectIdsByTeacherId(teacherId, tenantId);

            // Build subject info list
            List<TeacherSubjectInfoResponse.SubjectInfo> subjectInfos = subjectIds.stream()
                    .map(subjectId -> {
                        Subject subject = subjectRepository.findById(subjectId).orElse(null);
                        return TeacherSubjectInfoResponse.SubjectInfo.builder()
                                .subjectId(subjectId)
                                .subjectName(subject != null ? subject.getName() : "Unknown")
                                .subjectCode(subject != null ? subject.getCode() : null)
                                .build();
                    })
                    .collect(Collectors.toList());

            // Build section info with subjects taught in each section
            Map<UUID, List<String>> sectionSubjectsMap = assignments.stream()
                    .collect(Collectors.groupingBy(
                            TeacherClass::getSectionId,
                            Collectors.mapping(
                                    a -> {
                                        Subject s = a.getSubjectId() != null ?
                                                subjectRepository.findById(a.getSubjectId()).orElse(null) : null;
                                        return s != null ? s.getName() : "Unknown";
                                    },
                                    Collectors.toList()
                            )
                    ));

            List<TeacherSubjectInfoResponse.SectionInfo> sectionInfos = sectionSubjectsMap.entrySet().stream()
                    .map(entry -> {
                        UUID sectionId = entry.getKey();
                        Section section = sectionRepository.findById(sectionId).orElse(null);
                        return TeacherSubjectInfoResponse.SectionInfo.builder()
                                .sectionId(sectionId)
                                .sectionName(section != null ? section.getName() : "Unknown")
                                .className(section != null && section.getSchoolClass() != null ?
                                        section.getSchoolClass().getName() : null)
                                .subjectsInSection(entry.getValue())
                                .build();
                    })
                    .collect(Collectors.toList());

            return TeacherSubjectInfoResponse.builder()
                    .teacherId(teacherId)
                    .teacherName(teacher.getFullName())
                    .totalSubjectsCount((int) uniqueSubjectsCount)
                    .totalAssignmentsCount(assignments.size())
                    .subjects(subjectInfos)
                    .sections(sectionInfos)
                    .build();

        } catch (ResourceNotFoundException e) {
            log.warn("Teacher not found for subject info: {}", teacherId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching teacher subject info for {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching teacher subject info", e);
        }
    }

    /**
     * Converts entities to TeacherAssignmentResponse DTO.
     */
    private TeacherAssignmentResponse toTeacherAssignmentResponse(TeacherClass assignment,
                                                                   Teacher teacher,
                                                                   Section section,
                                                                   Subject subject,
                                                                   AcademicYear academicYear) {
        return TeacherAssignmentResponse.builder()
                .id(assignment.getId())
                .teacherId(assignment.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .sectionId(assignment.getSectionId())
                .sectionName(section != null ? section.getName() : null)
                .subjectId(assignment.getSubjectId())
                .subjectName(subject != null ? subject.getName() : null)
                .academicYearId(assignment.getAcademicYearId())
                .academicYearName(academicYear != null ? academicYear.getName() : null)
                .isActive(assignment.getIsActive())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
