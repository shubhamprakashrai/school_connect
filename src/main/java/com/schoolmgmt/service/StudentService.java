package com.schoolmgmt.service;


import com.schoolmgmt.dto.common.EmergencyContact;
import com.schoolmgmt.dto.common.ParentInfo;
import com.schoolmgmt.dto.request.CreateStudentRequest;
import com.schoolmgmt.dto.request.StudentFilterRequest;
import com.schoolmgmt.dto.request.UpdateStudentRequest;
import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.dto.response.StudentStatistics;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.StudentRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for student management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Create a new student
     */
    public StudentResponse createStudent(CreateStudentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Validate roll number uniqueness in class
        if (studentRepository.existsByRollNumberAndCurrentClassIdAndTenantId(
                request.getRollNumber(), request.getCurrentClassId(), tenantId)) {
            throw new BusinessException("Roll number already exists in this class: " + request.getRollNumber());
        }

        // Create student entity
        Student student = Student.builder()
                .rollNumber(request.getRollNumber())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(Student.Gender.valueOf(request.getGender()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .currentClassId(request.getCurrentClassId())
                .currentSectionId(UUID.fromString(request.getCurrentSectionId()))
                .admissionDate(request.getAdmissionDate())
                .previousSchool(request.getPreviousSchool())
                .status(Student.StudentStatus.ACTIVE)
                .build();

        // Set parent information
        if (request.getFatherInfo() != null) {
            student.setFatherName(request.getFatherInfo().getName());
            student.setFatherOccupation(request.getFatherInfo().getOccupation());
            student.setFatherPhone(request.getFatherInfo().getPhone());
            student.setFatherEmail(request.getFatherInfo().getEmail());
        }

        if (request.getMotherInfo() != null) {
            student.setMotherName(request.getMotherInfo().getName());
            student.setMotherOccupation(request.getMotherInfo().getOccupation());
            student.setMotherPhone(request.getMotherInfo().getPhone());
            student.setMotherEmail(request.getMotherInfo().getEmail());
        }

        if (request.getGuardianInfo() != null) {
            student.setGuardianName(request.getGuardianInfo().getName());
            student.setGuardianPhone(request.getGuardianInfo().getPhone());
            student.setGuardianEmail(request.getGuardianInfo().getEmail());
        }

        // Set emergency contact
        if (request.getEmergencyContact() != null) {
            student.setEmergencyContactName(request.getEmergencyContact().getName());
            student.setEmergencyContactRelation(request.getEmergencyContact().getRelation());
            student.setEmergencyContactPhone(request.getEmergencyContact().getPhone());
        }

        // Set medical information
        if (request.getMedicalInfo() != null) {
            student.setMedicalConditions(request.getMedicalInfo().getMedicalConditions());
            student.setDoctorName(request.getMedicalInfo().getDoctorName());
        }



        student.setTenantId(tenantId);

        // STEP 1: Save student first to get the generated ID
        Student savedStudent = studentRepository.save(student);
        log.info("Student created: {} - {} in tenant: {}",
                savedStudent.getRollNumber(), savedStudent.getFullName(), tenantId);

        // STEP 2: Create user account after student is saved (if requested)
        if (request.isCreateUserAccount() && request.getEmail() != null) {
            try {
                User user = createUserForStudent(savedStudent); // Now student.getId() is available!
                savedStudent.setUser(user);

                // Update student with user reference
                savedStudent = studentRepository.save(savedStudent);
                log.info("User account created for student: {}", savedStudent.getRollNumber());

            } catch (Exception e) {
                log.error("Failed to create user account for student: {}", savedStudent.getRollNumber(), e);
                // Student is already created, but user creation failed
                // You might want to handle this scenario based on your business logic
                // Option 1: Continue without user account
                // Option 2: Rollback student creation (if critical)
                throw new BusinessException("Student created but failed to create user account: " + e.getMessage());
            }
        }

        return toStudentResponse(savedStudent);
    }

    /**
     * Update student information
     */
    public StudentResponse updateStudent(UUID studentId, UpdateStudentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Verify tenant access
        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        // Update basic information
        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            student.setMiddleName(request.getMiddleName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            student.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            student.setCity(request.getCity());
        }
        if (request.getState() != null) {
            student.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            student.setPostalCode(request.getPostalCode());
        }
        if (request.getPhotoUrl() != null) {
            student.setPhotoUrl(request.getPhotoUrl());
        }

        // Update medical information
        if (request.getMedicalInfo() != null) {
            student.setMedicalConditions(request.getMedicalInfo().getMedicalConditions());
            student.setDoctorName(request.getMedicalInfo().getDoctorName());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student updated: {} - {}", updatedStudent.getRollNumber(), updatedStudent.getFullName());

        return toStudentResponse(updatedStudent);
    }

    /**
     * Get student by ID
     */
    public StudentResponse getStudentById(UUID studentId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        // Verify tenant access
        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }
        
        return toStudentResponse(student);
    }

    /**
     * Get all students with filtering and pagination
     */
    public Page<StudentResponse> getAllStudents(StudentFilterRequest filter, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Specification<Student> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)
        );
        
        // Add filters
        if (filter != null) {
            if (filter.getClassId() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("currentClassId"), filter.getClassId()));
            }
            if (filter.getSectionId() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("currentSectionId"), filter.getSectionId()));
            }
            if (filter.getStatus() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("status"), Student.StudentStatus.valueOf(filter.getStatus())));
            }
            if (filter.getGender() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("gender"), Student.Gender.valueOf(filter.getGender())));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), searchTerm),
                    cb.like(cb.lower(root.get("lastName")), searchTerm),
                    cb.like(cb.lower(root.get("rollNumber")), searchTerm)
                ));
            }
        }
        
        Page<Student> students = studentRepository.findAll(spec, pageable);
        return students.map(this::toStudentResponse);
    }

    /**
     * Update student status
     */
    public void updateStudentStatus(UUID studentId, String status) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        // Verify tenant access
        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }
        
        Student.StudentStatus studentStatus = Student.StudentStatus.valueOf(status.toUpperCase());
        studentRepository.updateStatus(studentId, studentStatus);
        
        log.info("Student status updated: {} to {}", student.getRollNumber(), status);
    }

    /**
     * Delete student (soft delete)
     */
    public void deleteStudent(UUID studentId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        
        // Verify tenant access
        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }
        
        student.softDelete(tenantId);
        studentRepository.save(student);
        
        log.info("Student soft deleted: {}", student.getRollNumber());
    }

    /**
     * Get student statistics
     */
    public StudentStatistics getStudentStatistics() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        long totalStudents = studentRepository.countByTenantIdAndStatus(tenantId, null);
        long activeStudents = studentRepository.countByTenantIdAndStatus(tenantId, Student.StudentStatus.ACTIVE);
        
        // Get statistics by class
        List<Object[]> classStats = studentRepository.getStudentStatisticsByClass(tenantId);
        Map<String, Long> studentsByClass = new HashMap<>();
        long maleStudents = 0;
        long femaleStudents = 0;
        
        for (Object[] stat : classStats) {
            String classId = (String) stat[0];
            Long count = (Long) stat[1];
            Long males = (Long) stat[2];
            Long females = (Long) stat[3];
            
            studentsByClass.put(classId, count);
            maleStudents += males;
            femaleStudents += females;
        }
        
        // Get statistics by status
        Map<String, Long> studentsByStatus = new HashMap<>();
        for (Student.StudentStatus status : Student.StudentStatus.values()) {
            long count = studentRepository.countByTenantIdAndStatus(tenantId, status);
            if (count > 0) {
                studentsByStatus.put(status.name(), count);
            }
        }
        
        return StudentStatistics.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .maleStudents(maleStudents)
                .femaleStudents(femaleStudents)
                .studentsByClass(studentsByClass)
                .studentsByStatus(studentsByStatus)
                .build();
    }

    /**
     * Create user account for student
     */
    private User createUserForStudent(Student student) {
        String username = generateUsername(student);
        String defaultPassword = generateDefaultPassword();
        
        User user = User.builder()
                .email(student.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .isActive(true)
                .referenceId(student.getId().toString())
                .referenceType("STUDENT")
                .build();
        
        user.setTenantId(student.getTenantId());
        User savedUser = userRepository.save(user);
        
        // Send welcome email with credentials
        // emailService.sendStudentCredentials(student, username, defaultPassword);
        
        return savedUser;
    }

    /**
     * Generate username for student
     */
    private String generateUsername(Student student) {
        String base = student.getFirstName().toLowerCase() + "." + 
                     student.getLastName().toLowerCase();
        base = base.replaceAll("[^a-z0-9.]", "");
        
        String username = base;
        int counter = 1;
        
        while (userRepository.existsByUsernameAndTenantId(username, student.getTenantId())) {
            username = base + counter;
            counter++;
        }
        
        return username;
    }

    /**
     * Generate default password
     */
    private String generateDefaultPassword() {
        return "Student@" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Convert Student entity to StudentResponse DTO
     */
    private StudentResponse toStudentResponse(Student student) {
        ParentInfo fatherInfo = null;
        if (student.getFatherName() != null) {
            fatherInfo = ParentInfo.builder()
                    .name(student.getFatherName())
                    .occupation(student.getFatherOccupation())
                    .phone(student.getFatherPhone())
                    .email(student.getFatherEmail())
                    .build();
        }
        
        ParentInfo motherInfo = null;
        if (student.getMotherName() != null) {
            motherInfo = ParentInfo.builder()
                    .name(student.getMotherName())
                    .occupation(student.getMotherOccupation())
                    .phone(student.getMotherPhone())
                    .email(student.getMotherEmail())
                    .build();
        }
        
        ParentInfo guardianInfo = null;
        if (student.getGuardianName() != null) {
            guardianInfo = ParentInfo.builder()
                    .name(student.getGuardianName())
                    .phone(student.getGuardianPhone())
                    .email(student.getGuardianEmail())
                    .build();
        }
        
        EmergencyContact emergencyContact = EmergencyContact.builder()
                .name(student.getEmergencyContactName())
                .relation(student.getEmergencyContactRelation())
                .phone(student.getEmergencyContactPhone())
                .build();
        
        return StudentResponse.builder()
                .id(student.getId().toString())
                .rollNumber(student.getRollNumber())
                .firstName(student.getFirstName())
                .middleName(student.getMiddleName())
                .lastName(student.getLastName())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender().name())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .city(student.getCity())
                .state(student.getState())
                .postalCode(student.getPostalCode())
                .currentClassId(student.getCurrentClassId())
                .currentSectionId(student.getCurrentSectionId().toString())
                .admissionDate(student.getAdmissionDate())
                .status(student.getStatus().name())
                .photoUrl(student.getPhotoUrl())
                .fatherInfo(fatherInfo)
                .motherInfo(motherInfo)
                .guardianInfo(guardianInfo)
                .emergencyContact(emergencyContact)
                .build();
    }
}
