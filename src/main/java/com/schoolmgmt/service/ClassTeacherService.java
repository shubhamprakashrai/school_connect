package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.ClassTeacherAssignmentRequest;
import com.schoolmgmt.dto.response.ClassTeacherResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ClassTeacherException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.repository.SectionRepository;
import com.schoolmgmt.repository.TeacherRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing class teacher assignments.
 * Handles the assignment of teachers as class teachers to specific sections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassTeacherService {

    private final SectionRepository sectionRepository;
    private final TeacherRepository teacherRepository;

    /**
     * Assigns a teacher as the class teacher for a section.
     *
     * @param request the assignment request containing section and teacher IDs
     * @return the assignment response with details
     * @throws ResourceNotFoundException if section or teacher not found
     * @throws BusinessException         if teacher is not eligible to be class teacher
     */
    @Transactional
    public ClassTeacherResponse assignClassTeacher(ClassTeacherAssignmentRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Starting class teacher assignment for tenant: {}, section: {}, teacher: {}",
                    tenantId, request.getSectionId(), request.getTeacherId());

            // Validate and fetch section
            Section section = sectionRepository.findByIdAndTenantId(request.getSectionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

            // Validate and fetch teacher
            Teacher teacher = teacherRepository.findByIdAndTenantId(request.getTeacherId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

            // Validate teacher is active and eligible
            validateTeacherForClassTeacher(teacher);

            // Check if section already has a class teacher
            if (section.getClassTeacher() != null && !section.getClassTeacher().getId().equals(teacher.getId())) {
                Teacher currentClassTeacher = section.getClassTeacher();
                log.info("Replacing existing class teacher {} with new teacher {} for section {}",
                        currentClassTeacher.getId(), teacher.getId(), section.getId());

                // Update the old teacher's class teacher status
                currentClassTeacher.setIsClassTeacher(false);
                currentClassTeacher.setClassTeacherFor(null);
                teacherRepository.save(currentClassTeacher);
            }

            // Assign new class teacher
            section.setClassTeacher(teacher);
            Section savedSection = sectionRepository.save(section);

            // Update teacher's class teacher status
            teacher.setIsClassTeacher(true);
            teacher.setClassTeacherFor(section.getName());
            teacherRepository.save(teacher);

            log.info("Class teacher assigned successfully: teacher {} assigned to section {} for tenant {}",
                    teacher.getId(), section.getId(), tenantId);

            return toClassTeacherResponse(savedSection, teacher);

        } catch (ResourceNotFoundException | BusinessException e) {
            log.warn("Business exception while assigning class teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while assigning class teacher for tenant {}: {}",
                    TenantContext.getCurrentTenant(), e.getMessage(), e);
            throw new InternalServiceException("Internal server error while assigning class teacher", e);
        }
    }

    /**
     * Removes the class teacher assignment from a section.
     *
     * @param sectionId the section ID
     * @throws ResourceNotFoundException if section not found
     */
    @Transactional
    public void removeClassTeacher(UUID sectionId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Removing class teacher from section: {} for tenant: {}", sectionId, tenantId);

            Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

            Teacher currentClassTeacher = section.getClassTeacher();
            if (currentClassTeacher == null) {
                log.warn("Section {} does not have a class teacher assigned", sectionId);
                throw new ClassTeacherException("Section does not have a class teacher assigned");
            }

            // Update teacher's class teacher status
            currentClassTeacher.setIsClassTeacher(false);
            currentClassTeacher.setClassTeacherFor(null);
            teacherRepository.save(currentClassTeacher);

            // Remove class teacher from section
            section.setClassTeacher(null);
            sectionRepository.save(section);

            log.info("Class teacher removed successfully from section: {}. Teacher: {} was unassigned.",
                    sectionId, currentClassTeacher.getId());

        } catch (ResourceNotFoundException | ClassTeacherException e) {
            log.warn("Exception while removing class teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error removing class teacher from section {}: {}", sectionId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while removing class teacher", e);
        }
    }

    /**
     * Gets the class teacher details for a section.
     *
     * @param sectionId the section ID
     * @return the class teacher response
     * @throws ResourceNotFoundException if section not found
     * @throws ClassTeacherException     if section has no class teacher assigned
     */
    @Transactional(readOnly = true)
    public ClassTeacherResponse getClassTeacherBySection(UUID sectionId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching class teacher for section: {} tenant: {}", sectionId, tenantId);

            Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

            Teacher classTeacher = section.getClassTeacher();
            if (classTeacher == null) {
                log.warn("No class teacher assigned to section: {}", sectionId);
                throw new ClassTeacherException("No class teacher assigned to this section");
            }

            log.debug("Found class teacher: {} for section: {}", classTeacher.getId(), sectionId);
            return toClassTeacherResponse(section, classTeacher);

        } catch (ResourceNotFoundException | ClassTeacherException e) {
            log.warn("Exception while fetching class teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching class teacher for section {}: {}", sectionId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching class teacher", e);
        }
    }

    /**
     * Gets all sections where a teacher is assigned as class teacher.
     *
     * @param teacherId the teacher ID
     * @return list of class teacher assignments
     * @throws ResourceNotFoundException if teacher not found
     */
    @Transactional(readOnly = true)
    public List<ClassTeacherResponse> getSectionsByClassTeacher(UUID teacherId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching sections where teacher {} is class teacher for tenant: {}", teacherId, tenantId);

            Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            List<Section> sections = sectionRepository.findByClassTeacherIdAndTenantId(teacherId, tenantId);

            log.debug("Found {} sections where teacher {} is class teacher", sections.size(), teacherId);

            return sections.stream()
                    .map(section -> toClassTeacherResponse(section, teacher))
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            log.warn("Exception while fetching sections by class teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching sections for teacher {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching sections by class teacher", e);
        }
    }

    /**
     * Gets all class teacher assignments for the current tenant.
     *
     * @param pageable pagination information
     * @return page of class teacher responses
     */
    @Transactional(readOnly = true)
    public Page<ClassTeacherResponse> getAllClassTeachers(Pageable pageable) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching all class teacher assignments for tenant: {}", tenantId);

            Page<Section> sections = sectionRepository.findByClassTeacherIsNotNullAndTenantId(tenantId, pageable);

            List<ClassTeacherResponse> responses = sections.getContent().stream()
                    .filter(section -> section.getClassTeacher() != null)
                    .map(section -> toClassTeacherResponse(section, section.getClassTeacher()))
                    .collect(Collectors.toList());

            log.debug("Found {} class teacher assignments for tenant: {}", responses.size(), tenantId);

            return new PageImpl<>(responses, pageable, sections.getTotalElements());

        } catch (Exception e) {
            log.error("Error fetching all class teachers for tenant {}: {}",
                    TenantContext.getCurrentTenant(), e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching class teachers", e);
        }
    }

    /**
     * Transfers class teacher from one section to another.
     *
     * @param fromSectionId the source section ID
     * @param toSectionId   the target section ID
     * @return the new assignment response
     */
    @Transactional
    public ClassTeacherResponse transferClassTeacher(UUID fromSectionId, UUID toSectionId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Transferring class teacher from section {} to section {} for tenant: {}",
                    fromSectionId, toSectionId, tenantId);

            // Get source section with class teacher
            Section fromSection = sectionRepository.findByIdAndTenantId(fromSectionId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", fromSectionId));

            Teacher teacher = fromSection.getClassTeacher();
            if (teacher == null) {
                throw new ClassTeacherException("Source section does not have a class teacher assigned");
            }

            // Remove from current section
            fromSection.setClassTeacher(null);
            sectionRepository.save(fromSection);

            // Create assignment request for target section
            ClassTeacherAssignmentRequest request = ClassTeacherAssignmentRequest.builder()
                    .sectionId(toSectionId)
                    .teacherId(teacher.getId())
                    .build();

            ClassTeacherResponse response = assignClassTeacher(request);

            log.info("Class teacher transferred successfully from {} to {}", fromSectionId, toSectionId);
            return response;

        } catch (ResourceNotFoundException | ClassTeacherException e) {
            log.warn("Exception while transferring class teacher: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error transferring class teacher from {} to {}: {}", fromSectionId, toSectionId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while transferring class teacher", e);
        }
    }

    /**
     * Handles cascade delete when a teacher is removed from the school.
     * This method should be called when deleting a teacher to remove their class teacher assignments.
     *
     * @param teacherId the teacher ID being deleted
     * @param tenantId  the tenant ID for validation
     */
    @Transactional
    public void handleTeacherDeletion(UUID teacherId, String tenantId) {
        try {
            log.info("Handling class teacher cleanup for deleted teacher: {} in tenant: {}", teacherId, tenantId);

            List<Section> sections = sectionRepository.findByClassTeacherIdAndTenantId(teacherId, tenantId);

            if (sections.isEmpty()) {
                log.debug("Teacher {} was not assigned as class teacher to any section", teacherId);
                return;
            }

            for (Section section : sections) {
                section.setClassTeacher(null);
                sectionRepository.save(section);
                log.info("Removed class teacher {} from section {}", teacherId, section.getId());
            }

            log.info("Successfully removed class teacher assignments for teacher: {} from {} sections",
                    teacherId, sections.size());

        } catch (Exception e) {
            log.error("Error handling teacher deletion for class teacher assignments: {}", e.getMessage(), e);
            throw new InternalServiceException("Failed to cleanup class teacher assignments", e);
        }
    }

    /**
     * Validates if a teacher is eligible to be a class teacher.
     *
     * @param teacher the teacher to validate
     * @throws BusinessException if teacher is not eligible
     */
    private void validateTeacherForClassTeacher(Teacher teacher) {
        if (!teacher.isActive()) {
            log.warn("Teacher {} is not active and cannot be assigned as class teacher", teacher.getId());
            throw new BusinessException("Teacher must be active to be assigned as class teacher");
        }

        if (teacher.getStatus() == Teacher.TeacherStatus.ON_LEAVE ||
            teacher.getStatus() == Teacher.TeacherStatus.RESIGNED ||
            teacher.getStatus() == Teacher.TeacherStatus.TERMINATED ||
            teacher.getStatus() == Teacher.TeacherStatus.RETIRED ||
            teacher.getStatus() == Teacher.TeacherStatus.TRANSFERRED) {
            log.warn("Teacher {} has status {} and cannot be assigned as class teacher",
                    teacher.getId(), teacher.getStatus());
            throw new BusinessException("Teacher with status '" + teacher.getStatus() + "' cannot be assigned as class teacher");
        }
    }

    /**
     * Converts Section and Teacher entities to ClassTeacherResponse DTO.
     *
     * @param section the section entity
     * @param teacher the teacher entity
     * @return the response DTO
     */
    private ClassTeacherResponse toClassTeacherResponse(Section section, Teacher teacher) {
        return ClassTeacherResponse.builder()
                .sectionId(section.getId())
                .sectionName(section.getName())
                .className(section.getSchoolClass() != null ? section.getSchoolClass().getName() : null)
                .teacherId(teacher.getId())
                .teacherName(teacher.getFullName())
                .employeeId(teacher.getEmployeeId())
                .assignedAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
