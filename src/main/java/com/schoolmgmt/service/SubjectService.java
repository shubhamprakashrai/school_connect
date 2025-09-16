package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.SubjectCreationRequest;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.exception.DuplicateResourceException;
import com.schoolmgmt.model.Subject;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.repository.SubjectRepository;
import com.schoolmgmt.repository.SchoolClassRepository;
import com.schoolmgmt.repository.TeacherRepository;
import com.schoolmgmt.repository.TeacherSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    @Transactional
    public Subject createSubject(SubjectCreationRequest request) {
        log.info("Creating subject: {}", request.getName());

        // Check if subject code already exists
        if (subjectRepository.existsByCodeAndTenantId(request.getCode(), getCurrentTenantId())) {
            throw new DuplicateResourceException("Subject with code '" + request.getCode() + "' already exists");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .type(Subject.SubjectType.valueOf(request.getType() != null ? request.getType() : "CORE"))
                .creditHours(request.getCreditHours())
                .maxMarks(request.getMaxMarks())
                .passingMarks(request.getPassingMarks())
                .academicYear(request.getAcademicYear())
                .department(request.getDepartment())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .prerequisites(request.getPrerequisites())
                .learningObjectives(request.getLearningObjectives())
                .build();

        Subject savedSubject = subjectRepository.save(subject);

        // Assign to classes if provided
        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            assignToClasses(savedSubject.getId(), request.getClassIds());
        }

        // Assign to teachers if provided
        if (request.getTeacherIds() != null && !request.getTeacherIds().isEmpty()) {
            assignToTeachers(savedSubject.getId(), request.getTeacherIds());
        }

        return savedSubject;
    }

    @Transactional
    public List<Subject> createMultipleSubjects(List<SubjectCreationRequest> requests) {
        log.info("Creating {} subjects", requests.size());
        return requests.stream()
                .map(this::createSubject)
                .collect(Collectors.toList());
    }

    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAllByTenantId(getCurrentTenantId(), pageable);
    }

    public Subject getSubjectById(UUID id) {
        return subjectRepository.findByIdAndTenantId(id, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    public Subject getSubjectByCode(String code) {
        return subjectRepository.findByCodeAndTenantId(code, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "code", code));
    }

    public List<Subject> getSubjectsByClass(UUID classId) {
        // Verify class exists
        schoolClassRepository.findByIdAndTenantId(classId, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        return subjectRepository.findByClassId(classId);
    }

    public List<Subject> getSubjectsByTeacher(UUID teacherId) {
        // Verify teacher exists
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        return subjectRepository.findByTeacherId(teacherId);
    }

    public Page<Subject> searchSubjects(String query, Pageable pageable) {
        return subjectRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndTenantId(
                query, query, getCurrentTenantId(), pageable);
    }

    @Transactional
    public Subject updateSubject(UUID id, SubjectCreationRequest request) {
        log.info("Updating subject: {}", id);
        
        Subject subject = getSubjectById(id);

        // Check if new code conflicts with existing subjects (excluding current one)
        if (!subject.getCode().equals(request.getCode()) && 
            subjectRepository.existsByCodeAndTenantId(request.getCode(), getCurrentTenantId())) {
            throw new DuplicateResourceException("Subject with code '" + request.getCode() + "' already exists");
        }

        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setDescription(request.getDescription());
        subject.setType(Subject.SubjectType.valueOf(request.getType() != null ? request.getType() : "CORE"));
        subject.setCreditHours(request.getCreditHours());
        subject.setMaxMarks(request.getMaxMarks());
        subject.setPassingMarks(request.getPassingMarks());
        subject.setAcademicYear(request.getAcademicYear());
        subject.setDepartment(request.getDepartment());
        subject.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        subject.setPrerequisites(request.getPrerequisites());
        subject.setLearningObjectives(request.getLearningObjectives());

        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(UUID id) {
        log.info("Deleting subject: {}", id);
        
        Subject subject = getSubjectById(id);
        subject.setActive(false);
        subject.setDeleted(true);
        
        subjectRepository.save(subject);
    }

    @Transactional
    public void assignSubjectToClass(UUID subjectId, UUID classId) {
        log.info("Assigning subject {} to class {}", subjectId, classId);
        
        Subject subject = getSubjectById(subjectId);
        SchoolClass schoolClass = schoolClassRepository.findByIdAndTenantId(classId, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

        // Add class to subject's classes (assuming Many-to-Many relationship)
        subject.getClasses().add(schoolClass);
        subjectRepository.save(subject);
    }

    @Transactional
    public void assignSubjectToTeacher(UUID subjectId, UUID teacherId) {
        log.info("Assigning subject {} to teacher {}", subjectId, teacherId);
        
        Subject subject = getSubjectById(subjectId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

        // Create teacher-subject assignment using repository
        teacherSubjectRepository.assignTeacherToSubject(teacherId, subjectId, getCurrentTenantId());
    }

    @Transactional
    public void removeSubjectFromClass(UUID subjectId, UUID classId) {
        log.info("Removing subject {} from class {}", subjectId, classId);
        
        Subject subject = getSubjectById(subjectId);
        SchoolClass schoolClass = schoolClassRepository.findByIdAndTenantId(classId, getCurrentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

        subject.getClasses().remove(schoolClass);
        subjectRepository.save(subject);
    }

    @Transactional
    public void removeSubjectFromTeacher(UUID subjectId, UUID teacherId) {
        log.info("Removing subject {} from teacher {}", subjectId, teacherId);
        
        teacherSubjectRepository.removeTeacherFromSubject(teacherId, subjectId, getCurrentTenantId());
    }

    private void assignToClasses(UUID subjectId, List<UUID> classIds) {
        classIds.forEach(classId -> assignSubjectToClass(subjectId, classId));
    }

    private void assignToTeachers(UUID subjectId, List<UUID> teacherIds) {
        teacherIds.forEach(teacherId -> assignSubjectToTeacher(subjectId, teacherId));
    }

    private String getCurrentTenantId() {
        // This should be implemented to get current tenant from security context
        // For now, returning a placeholder
        return com.schoolmgmt.util.TenantContext.getCurrentTenant();
    }
}