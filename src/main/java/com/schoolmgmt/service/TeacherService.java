package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Teacher createTeacher(TeacherCreationRequest request) {
        // In a real app, you'd also check if the email is already taken for the current tenant.

        // 1. Create the User account for authentication.
        // A temporary password can be generated and sent via email.
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Generate a secure random password
                .role(User.UserRole.TEACHER)
                .isActive(true)
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

        // In a real app, you would send a welcome email to the teacher with their login details.

        return teacherRepository.save(teacher);
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

    public Page<Teacher> getAllTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable);
    }

    public Teacher getTeacherById(UUID id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    public Teacher getTeacherByEmployeeId(String employeeId) {
        return teacherRepository.findByEmployeeId(employeeId)
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
        teacher.setStatus(Teacher.TeacherStatus.INACTIVE);
        teacher.setDeleted(true);
        
        // Deactivate user account
        if (teacher.getUser() != null) {
            User user = teacher.getUser();
            user.setActive(false);
            userRepository.save(user);
        }
        
        teacherRepository.save(teacher);
    }

    public List<TeacherClass> getTeacherAssignments(UUID teacherId) {
        return teacherClassRepository.findByTeacherId(teacherId);
    }
}