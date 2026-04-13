package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import com.schoolmgmt.util.TenantIdFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.name:School Connect}")
    private String appName;

    public String generateNextEmployeeId() {
        String tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findByIdentifier(tenantId).orElse(null);
        String initials = tenant != null
                ? TenantIdFormatter.extractInitials(tenant.getName())
                : "SC";
        int nextSeq = (int) teacherRepository.countByTenantIdAndStatus(tenantId, Teacher.TeacherStatus.ACTIVE) + 1;
        return TenantIdFormatter.generateEmployeeId(initials, nextSeq);
    }

    @Transactional
    public Teacher createTeacher(TeacherCreationRequest request) {
        // In a real app, you'd also check if the email is already taken for the current tenant.

        // 1. Create the User account for authentication.
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .role(User.UserRole.TEACHER)
                .isActive(true)
                .tempPasswordForFirstTime(tempPassword)
                .build();
        User savedUser = userRepository.save(user);

        // 2. Create the Teacher profile with domain-specific info.
        Teacher teacher = Teacher.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .address(request.getAddress())
                .designation(request.getDesignation())
                .joiningDate(request.getJoiningDate())
                .status(Teacher.TeacherStatus.ACTIVE)
                .user(savedUser) // Link the profile to the user account
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);

        // Send credentials email to the teacher
        try {
            String schoolName = tenantRepository.findByIdentifier(TenantContext.getCurrentTenant())
                    .map(Tenant::getName).orElse(appName);
            emailService.sendTeacherCredentials(
                    savedTeacher.getEmail(), savedTeacher.getFirstName(),
                    savedTeacher.getEmployeeId(), tempPassword, schoolName);
        } catch (Exception e) {
            log.warn("Failed to send teacher credentials email to: {}", savedTeacher.getEmail(), e);
        }

        return savedTeacher;
    }

    @Transactional
    public TeacherClass assignTeacherToClass(TeacherAssignmentRequest request) {
        // 1. Fetch all related entities by their UUIDs.
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

        // 2. Create the TeacherClass assignment entity.
        TeacherClass assignment = TeacherClass.builder()
                .teacherId(teacher.getId())
                .sectionId(section.getId())
                .subjectId(subject.getId())
                .academicYearId(academicYear.getId())
                .isActive(true)
                .build();

        return teacherClassRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public Page<Teacher> getAllTeachers(Pageable pageable) {
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        return teacherRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Teacher> getAllTeachersIncludingDeleted(Pageable pageable) {
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        return teacherRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllActiveTeachers() {
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        return teacherRepository.findByStatusAndTenantId(Teacher.TeacherStatus.ACTIVE, tenantId);
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherById(UUID id) {
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        return teacherRepository.findById(id)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherByEmployeeId(String employeeId) {
        String tenantId = com.schoolmgmt.util.TenantContext.requireCurrentTenant();
        return teacherRepository.findByEmployeeId(employeeId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "employeeId", employeeId));
    }

    @Transactional
    public Teacher updateTeacher(UUID id, TeacherCreationRequest request) {
        Teacher teacher = getTeacherById(id);
        
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setAddress(request.getAddress());
        teacher.setDesignation(request.getDesignation());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setGender(request.getGender());
        
        // Update associated user if needed
        if (teacher.getUser() != null) {
            User user = teacher.getUser();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }
        
        return teacherRepository.save(teacher);
    }

    @Transactional
    public void deleteTeacher(UUID id) {
        Teacher teacher = getTeacherById(id);

        if (Boolean.TRUE.equals(teacher.getIsDeleted())) {
            // Already soft-deleted → hard delete
            if (teacher.getUser() != null) {
                userRepository.delete(teacher.getUser());
            }
            teacherRepository.delete(teacher);
        } else {
            // First delete → soft delete
            teacher.setStatus(Teacher.TeacherStatus.INACTIVE);
            teacher.setDeleted(true);
            if (teacher.getUser() != null) {
                User user = teacher.getUser();
                user.setActive(false);
                userRepository.save(user);
            }
            teacherRepository.save(teacher);
        }
    }

    @Transactional(readOnly = true)
    public List<TeacherClass> getTeacherAssignments(UUID teacherId) {
        return teacherClassRepository.findByTeacherId(teacherId);
    }
}