package com.schoolmgmt.service;


import com.schoolmgmt.dto.request.CreateTeacherRequest;
import com.schoolmgmt.dto.request.TeacherFilterRequest;
import com.schoolmgmt.dto.request.UpdateTeacherRequest;
import com.schoolmgmt.dto.response.TeacherResponse;
import com.schoolmgmt.dto.response.TeacherStatistics;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TeacherRepository;
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

import java.time.LocalDate;
import java.util.*;

/**
 * Service for teacher management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Create a new teacher
     */
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        // Validate employee ID uniqueness
        if (teacherRepository.existsByEmployeeIdAndTenantId(request.getEmployeeId(), tenantId)) {
            throw new BusinessException("Employee ID already exists: " + request.getEmployeeId());
        }
        
        // Validate email uniqueness
        if (teacherRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }
        
        // Create teacher entity
        Teacher teacher = Teacher.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(Teacher.Gender.valueOf(request.getGender()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .joiningDate(request.getJoiningDate())
                .employeeType(Teacher.EmployeeType.valueOf(request.getEmployeeType()))
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .subjects(request.getSubjects() != null ? request.getSubjects() : new HashSet<>())
                .highestQualification(request.getHighestQualification())
                .professionalQualification(request.getProfessionalQualification())
                .experienceYears(request.getExperienceYears())
                .previousSchool(request.getPreviousSchool())
                .specializations(request.getSpecializations() != null ? request.getSpecializations() : new HashSet<>())
                .basicSalary(request.getBasicSalary())
                .status(Teacher.TeacherStatus.ACTIVE)
                .build();
        
        // Set bank details
        if (request.getBankDetails() != null) {
            teacher.setBankName(request.getBankDetails().getBankName());
            teacher.setBankAccountNumber(request.getBankDetails().getAccountNumber());
            teacher.setBankBranch(request.getBankDetails().getBranch());
            teacher.setIfscCode(request.getBankDetails().getIfscCode());
        }
        
        // Set emergency contact
        if (request.getEmergencyContact() != null) {
            teacher.setEmergencyContactName(request.getEmergencyContact().getName());
            teacher.setEmergencyContactRelation(request.getEmergencyContact().getRelation());
            teacher.setEmergencyContactPhone(request.getEmergencyContact().getPhone());
        }
        
        teacher.setTenantId(tenantId);
        
        // Create user account if requested
        if (request.isCreateUserAccount()) {
            User user = createUserForTeacher(teacher);
            teacher.setUser(user);
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher created: {} - {} in tenant: {}", 
                savedTeacher.getEmployeeId(), savedTeacher.getFullName(), tenantId);
        
        return toTeacherResponse(savedTeacher);
    }

    /**
     * Update teacher information
     */
    public TeacherResponse updateTeacher(UUID teacherId, UpdateTeacherRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Verify tenant access
        if (!teacher.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        
        // Update basic information
        if (request.getFirstName() != null) {
            teacher.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            teacher.setMiddleName(request.getMiddleName());
        }
        if (request.getLastName() != null) {
            teacher.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            // Check email uniqueness if changed
            if (!teacher.getEmail().equals(request.getEmail()) &&
                teacherRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                throw new BusinessException("Email already exists: " + request.getEmail());
            }
            teacher.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            teacher.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            teacher.setCity(request.getCity());
        }
        if (request.getState() != null) {
            teacher.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            teacher.setPostalCode(request.getPostalCode());
        }
        if (request.getDepartment() != null) {
            teacher.setDepartment(request.getDepartment());
        }
        if (request.getDesignation() != null) {
            teacher.setDesignation(request.getDesignation());
        }
        if (request.getSubjects() != null) {
            teacher.setSubjects(request.getSubjects());
        }
        if (request.getQualifications() != null) {
            teacher.setQualifications(request.getQualifications());
        }
        if (request.getPhotoUrl() != null) {
            teacher.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getResumeUrl() != null) {
            teacher.setResumeUrl(request.getResumeUrl());
        }
        
        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher updated: {} - {}", updatedTeacher.getEmployeeId(), updatedTeacher.getFullName());
        
        return toTeacherResponse(updatedTeacher);
    }

    /**
     * Get teacher by ID
     */
    public TeacherResponse getTeacherById(UUID teacherId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Verify tenant access
        if (!teacher.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        
        return toTeacherResponse(teacher);
    }

    /**
     * Get all teachers with filtering and pagination
     */
    public Page<TeacherResponse> getAllTeachers(TeacherFilterRequest filter, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Specification<Teacher> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)
        );
        
        // Add filters
        if (filter != null) {
            if (filter.getDepartment() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("department"), filter.getDepartment()));
            }
            if (filter.getDesignation() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("designation"), filter.getDesignation()));
            }
            if (filter.getStatus() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("status"), Teacher.TeacherStatus.valueOf(filter.getStatus())));
            }
            if (filter.getEmployeeType() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("employeeType"), Teacher.EmployeeType.valueOf(filter.getEmployeeType())));
            }
            if (filter.getClassTeachersOnly() != null && filter.getClassTeachersOnly()) {
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("isClassTeacher"), true));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), searchTerm),
                    cb.like(cb.lower(root.get("lastName")), searchTerm),
                    cb.like(cb.lower(root.get("employeeId")), searchTerm)
                ));
            }
        }
        
        Page<Teacher> teachers = teacherRepository.findAll(spec, pageable);
        return teachers.map(this::toTeacherResponse);
    }

    /**
     * Assign teacher as class teacher
     */
    public void assignClassTeacher(UUID teacherId, String classId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Verify tenant access
        if (!teacher.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        
        // Check if another teacher is already class teacher for this class
        Optional<Teacher> existingClassTeacher = teacherRepository.findByClassTeacherForAndTenantId(classId, tenantId);
        if (existingClassTeacher.isPresent() && !existingClassTeacher.get().getId().equals(teacherId)) {
            throw new BusinessException("Class already has a class teacher: " + existingClassTeacher.get().getFullName());
        }
        
        teacherRepository.assignClassTeacher(teacherId, classId);
        log.info("Teacher {} assigned as class teacher for class: {}", teacher.getEmployeeId(), classId);
    }

    /**
     * Remove class teacher assignment
     */
    public void removeClassTeacher(UUID teacherId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Verify tenant access
        if (!teacher.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        
        teacherRepository.removeClassTeacher(teacherId);
        log.info("Class teacher assignment removed for teacher: {}", teacher.getEmployeeId());
    }

    /**
     * Update teacher rating
     */
    public void updateTeacherRating(UUID teacherId, Double rating) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Verify tenant access
        if (!teacher.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }
        
        teacherRepository.updateRating(teacherId, rating, LocalDate.now());
        log.info("Teacher rating updated: {} - Rating: {}", teacher.getEmployeeId(), rating);
    }

    /**
     * Get teacher statistics
     */
    public TeacherStatistics getTeacherStatistics() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        long totalTeachers = teacherRepository.count();
        long activeTeachers = teacherRepository.countByTenantIdAndStatus(tenantId, Teacher.TeacherStatus.ACTIVE);
        
        // Get statistics by department
        List<Object[]> deptStats = teacherRepository.getTeacherStatisticsByDepartment(tenantId);
        Map<String, Long> teachersByDepartment = new HashMap<>();
        Map<String, Double> avgRatingByDepartment = new HashMap<>();
        double totalExperience = 0;
        int teacherCount = 0;
        
        for (Object[] stat : deptStats) {
            String department = (String) stat[0];
            Long count = (Long) stat[1];
            Double avgExperience = (Double) stat[2];
            Double avgRating = (Double) stat[3];
            
            if (department != null) {
                teachersByDepartment.put(department, count);
                if (avgRating != null) {
                    avgRatingByDepartment.put(department, avgRating);
                }
            }
            
            if (avgExperience != null) {
                totalExperience += avgExperience * count;
                teacherCount += count;
            }
        }
        
        // Get statistics by employee type
        Map<String, Long> teachersByEmployeeType = new HashMap<>();
        for (Teacher.EmployeeType type : Teacher.EmployeeType.values()) {
            long count = teacherRepository.findByEmployeeTypeAndTenantId(type, tenantId).size();
            if (count > 0) {
                teachersByEmployeeType.put(type.name(), count);
            }
        }
        
        // Count class teachers
        long classTeachers = teacherRepository.findAll().stream()
                .filter(t -> t.getTenantId().equals(tenantId) && Boolean.TRUE.equals(t.getIsClassTeacher()))
                .count();
        
        double averageExperience = teacherCount > 0 ? totalExperience / teacherCount : 0;
        
        return TeacherStatistics.builder()
                .totalTeachers(totalTeachers)
                .activeTeachers(activeTeachers)
                .teachersByDepartment(teachersByDepartment)
                .teachersByEmployeeType(teachersByEmployeeType)
                .averageRatingByDepartment(avgRatingByDepartment)
                .classTeachers(classTeachers)
                .averageExperience(averageExperience)
                .build();
    }

    /**
     * Create user account for teacher
     */
    private User createUserForTeacher(Teacher teacher) {
        String username = generateUsername(teacher);
        String defaultPassword = generateDefaultPassword();
        
        User user = User.builder()
                .username(username)
                .email(teacher.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .primaryRole(User.UserRole.TEACHER)
                .roles(Set.of(User.UserRole.TEACHER))
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .enabled(true)
                .referenceId(teacher.getId().toString())
                .referenceType("TEACHER")
                .build();
        
        user.setTenantId(teacher.getTenantId());
        User savedUser = userRepository.save(user);
        
        // Send welcome email with credentials
        // emailService.sendTeacherCredentials(teacher, username, defaultPassword);
        
        return savedUser;
    }

    /**
     * Generate username for teacher
     */
    private String generateUsername(Teacher teacher) {
        String base = teacher.getFirstName().toLowerCase() + "." + 
                     teacher.getLastName().toLowerCase();
        base = base.replaceAll("[^a-z0-9.]", "");
        
        String username = base;
        int counter = 1;
        
        while (userRepository.existsByUsernameAndTenantId(username, teacher.getTenantId())) {
            username = base + counter;
            counter++;
        }
        
        return username;
    }

    /**
     * Generate default password
     */
    private String generateDefaultPassword() {
        return "Teacher@" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Convert Teacher entity to TeacherResponse DTO
     */
    private TeacherResponse toTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId().toString())
                .employeeId(teacher.getEmployeeId())
                .firstName(teacher.getFirstName())
                .middleName(teacher.getMiddleName())
                .lastName(teacher.getLastName())
                .fullName(teacher.getFullName())
                .dateOfBirth(teacher.getDateOfBirth())
                .gender(teacher.getGender().name())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .address(teacher.getAddress())
                .city(teacher.getCity())
                .state(teacher.getState())
                .postalCode(teacher.getPostalCode())
                .joiningDate(teacher.getJoiningDate())
                .employeeType(teacher.getEmployeeType().name())
                .department(teacher.getDepartment())
                .designation(teacher.getDesignation())
                .subjects(teacher.getSubjects())
                .classes(teacher.getClasses())
                .isClassTeacher(teacher.getIsClassTeacher())
                .classTeacherFor(teacher.getClassTeacherFor())
                .highestQualification(teacher.getHighestQualification())
                .experienceYears(teacher.getExperienceYears())
                .status(teacher.getStatus().name())
                .photoUrl(teacher.getPhotoUrl())
                .rating(teacher.getRating())
                .age(teacher.getAge())
                .serviceYears(teacher.getServiceYears())
                .build();
    }
}
