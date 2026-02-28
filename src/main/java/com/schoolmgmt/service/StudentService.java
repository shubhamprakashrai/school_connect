package com.schoolmgmt.service;


import com.schoolmgmt.dto.common.EmergencyContact;
import com.schoolmgmt.dto.common.ParentInfo;
import com.schoolmgmt.dto.request.CreateStudentRequest;
import com.schoolmgmt.dto.request.ParentRequest;
import com.schoolmgmt.dto.request.StudentFilterRequest;
import com.schoolmgmt.dto.request.UpdateStudentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.dto.response.StudentStatistics;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import com.schoolmgmt.util.UserIdGeneratorBasedonTenantIdentifies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserService userService;
    private final TenantRepository tenantRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final ParentServiceImpl parentService;
    private final ParentRepository parentRepository;


    /**
     * Create a new student
     */
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Validate roll number uniqueness in class
        // Validate SchoolClass exists and belongs to tenant
        SchoolClass schoolClass = schoolClassRepository
                .findByIdAndTenantId(request.getSchoolClass().getId(), tenantId)
                .orElseThrow(() -> {
                    log.error("School class not found - ID: {}, Tenant: {}",
                            request.getSchoolClass().getId(), tenantId);
                    return new ResourceNotFoundException(
                            "School class not found with ID: " + request.getSchoolClass().getId());
                });

        // Validate Section exists and belongs to tenant
        Section section = sectionRepository
                .findByIdAndTenantId(request.getSection().getId(), tenantId)
                .orElseThrow(() -> {
                    log.error("Section not found - ID: {}, Tenant: {}",
                            request.getSection().getId(), tenantId);
                    return new ResourceNotFoundException(
                            "Section not found with ID: " + request.getSection().getId());
                });

        // Create user account for student
        User user = userService.createUser("STUDENT", request.getUserRequest(), tenantId);

        // Generate roll number automatically (use user ID like existing data)
        log.info("Using user ID as roll number for student in class {} tenant {}", schoolClass.getId(), tenantId);
        String rollNumber = user.getUserId(); // Use the same user ID as roll number

        // Check if this roll number already exists in the same class
        if (studentRepository.existsByRollNumberAndSchoolClassIdAndTenantId(rollNumber, schoolClass.getId(), tenantId)) {
            log.error("Roll number {} already exists in class {} for tenant {}", rollNumber, schoolClass.getId(), tenantId);
            throw new BusinessException("Roll number conflict: This user ID is already assigned to another student in the same class");
        }

        log.info("Generated roll number {} for class {} in tenant {} (from user ID: {})", rollNumber, schoolClass.getId(), tenantId, user.getUserId());

        // Build student entity
        Student student = Student.builder()
                .user(user)
                .schoolClass(schoolClass)
                .section(section)
                .rollNumber(rollNumber)
                .firstName(request.getUserRequest().getFirstName())
                .middleName(request.getUserRequest().getMiddleName())
                .lastName(request.getUserRequest().getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(Student.Gender.valueOf(request.getGender()))
                .email(request.getUserRequest().getEmail())
                .phone(request.getUserRequest().getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .admissionDate(request.getAdmissionDate())
                .status(Student.StudentStatus.ACTIVE)
                .fatherName(request.getFatherInfo() != null ? request.getFatherInfo().getName() : null)
                .fatherOccupation(request.getFatherInfo() != null ? request.getFatherInfo().getOccupation() : null)
                .fatherPhone(request.getFatherInfo() != null ? request.getFatherInfo().getPhone() : null)
                .fatherEmail(request.getFatherInfo() != null ? request.getFatherInfo().getEmail() : null)
                .motherName(request.getMotherInfo() != null ? request.getMotherInfo().getName() : null)
                .motherOccupation(request.getMotherInfo() != null ? request.getMotherInfo().getOccupation() : null)
                .motherPhone(request.getMotherInfo() != null ? request.getMotherInfo().getPhone() : null)
                .motherEmail(request.getMotherInfo() != null ? request.getMotherInfo().getEmail() : null)
                .guardianName(request.getGuardianInfo() != null ? request.getGuardianInfo().getName() : null)
                .guardianRelation(request.getGuardianInfo() != null ? request.getGuardianInfo().getParentType() : null)
                .guardianPhone(request.getGuardianInfo() != null ? request.getGuardianInfo().getPhone() : null)
                .guardianEmail(request.getGuardianInfo() != null ? request.getGuardianInfo().getEmail() : null)
                .emergencyContactName(request.getEmergencyContact() != null ? request.getEmergencyContact().getName() : null)
                .emergencyContactRelation(request.getEmergencyContact() != null ? request.getEmergencyContact().getRelation() : null)
                .emergencyContactPhone(request.getEmergencyContact() != null ? request.getEmergencyContact().getPhone() : null)
                .medicalConditions(request.getMedicalInfo() != null ? request.getMedicalInfo().getMedicalConditions() : null)
                .doctorName(request.getMedicalInfo() != null ? request.getMedicalInfo().getDoctorName() : null)
                .build();

        // Set tenant and other required fields
        student.setTenantId(tenantId);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        // Create parent records if provided (student will be saved within the same transaction)
        log.info("Starting student and parent creation process");

        try {
            // Save student first
            Student savedStudent = studentRepository.save(student);
            log.info("Student saved successfully: {} {}", savedStudent.getFirstName(), savedStudent.getLastName());

            log.info("Starting parent creation process for student: {}", savedStudent.getId());

            if (request.getFatherInfo() != null) {
                log.info("Father info provided: {}", request.getFatherInfo().getName());
                ParentRequest fatherRequest = toParentRequest(request.getFatherInfo(), "FATHER");
                if (fatherRequest != null) {
                    ParentResponse fatherResponse = parentService.createParent(fatherRequest);
                    // Link student to parent
                    linkStudentToParent(savedStudent, fatherResponse.getParentId().toString(), "PARENT");
                    log.info("Father parent created and linked: {}", fatherRequest.getEmail());
                }
            } else {
                log.info("No father info provided in request");
            }

            if (request.getMotherInfo() != null) {
                log.info("Mother info provided: {}", request.getMotherInfo().getName());
                ParentRequest motherRequest = toParentRequest(request.getMotherInfo(), "MOTHER");
                if (motherRequest != null) {
                    ParentResponse motherResponse = parentService.createParent(motherRequest);
                    // Link student to parent
                    linkStudentToParent(savedStudent, motherResponse.getParentId().toString(), "PARENT");
                    log.info("Mother parent created and linked: {}", motherRequest.getEmail());
                }
            } else {
                log.info("No mother info provided in request");
            }

            if (request.getGuardianInfo() != null) {
                log.info("Guardian info provided: {}", request.getGuardianInfo().getName());
                ParentRequest guardianRequest = toParentRequest(request.getGuardianInfo(), "GUARDIAN");
                if (guardianRequest != null) {
                    ParentResponse guardianResponse = parentService.createParent(guardianRequest);
                    // Link student to guardian
                    linkStudentToParent(savedStudent, guardianResponse.getParentId().toString(), "GUARDIAN");
                    log.info("Guardian parent created and linked: {}", guardianRequest.getEmail());
                }
            } else {
                log.info("No guardian info provided in request");
            }

            log.info("Student and parent creation process completed successfully for student: {}", savedStudent.getId());

            log.info("Student and parent creation process completed successfully for student: {}", savedStudent.getId());

            // Convert to response and return
            return StudentResponse.builder()
                    .id(savedStudent.getId().toString())
                    .firstName(savedStudent.getFirstName())
                    .middleName(savedStudent.getMiddleName())
                    .lastName(savedStudent.getLastName())
                    .rollNumber(savedStudent.getRollNumber())
                    .schoolClass(SchoolClassResponse.builder()
                            .id(schoolClass.getId())
                            .code(schoolClass.getClassIdentifier())
                            .name(schoolClass.getName())
                            .description(schoolClass.getDescription())
                            .build())
                    .section(SectionResponse.builder()
                            .id(section.getId())
                            .name(section.getName())
                            .capacity(section.getCapacity())
                            .build())
                    .dateOfBirth(savedStudent.getDateOfBirth())
                    .gender(savedStudent.getGender().name())
                    .email(savedStudent.getEmail())
                    .phone(savedStudent.getPhone())
                    .address(savedStudent.getAddress())
                    .city(savedStudent.getCity())
                    .state(savedStudent.getState())
                    .postalCode(savedStudent.getPostalCode())
                    .admissionDate(savedStudent.getAdmissionDate())
                    .status(savedStudent.getStatus().name())
                    .photoUrl(savedStudent.getPhotoUrl())
                    .fatherInfo(savedStudent.getFatherName() != null ? ParentInfo.builder().name(savedStudent.getFatherName()).occupation(savedStudent.getFatherOccupation()).phone(savedStudent.getFatherPhone()).email(savedStudent.getFatherEmail()).build() : null)
                    .motherInfo(savedStudent.getMotherName() != null ? ParentInfo.builder().name(savedStudent.getMotherName()).occupation(savedStudent.getMotherOccupation()).phone(savedStudent.getMotherPhone()).email(savedStudent.getMotherEmail()).build() : null)
                    .guardianInfo(savedStudent.getGuardianName() != null ? ParentInfo.builder().name(savedStudent.getGuardianName()).phone(savedStudent.getGuardianPhone()).email(savedStudent.getGuardianEmail()).build() : null)
                    .emergencyContact(savedStudent.getEmergencyContactName() != null ? EmergencyContact.builder().name(savedStudent.getEmergencyContactName()).relation(savedStudent.getEmergencyContactRelation()).phone(savedStudent.getEmergencyContactPhone()).build() : null)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create student or parent account: {}", e.getMessage(), e);
            // If anything fails, the entire operation fails
            throw new BusinessException("Failed to create student: " + e.getMessage());
        }

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

        // Start with a Specification for tenantId
        Specification<Student> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);

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
    @Transactional
    public void deleteStudent(UUID studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Student not found"));

        // 🔥 Fix for WARDS / Parent ↔ Student
        for (Parent p : student.getParents()) {
            p.getWards().remove(student);
        }
        student.getParents().clear();

        // 🔥 Fix for Guardians
        for (Parent g : student.getGuardians()) {
            g.getWards().remove(student);
        }
        student.getGuardians().clear();

        // 🔥 Break User ↔ Student
        if (student.getUser() != null) {
            userRepository.delete(student.getUser());
            student.setUser(null);
        }

        // 🔥 Finally delete student
        studentRepository.delete(student);
    }

    /**
     * Get students by section ID
     */
    public List<StudentResponse> getStudentsBySection(UUID sectionId) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        List<Student> students = studentRepository.findBySectionIdAndTenantId(sectionId, tenantId);
        return students.stream()
                .map(this::toStudentResponse)
                .collect(java.util.stream.Collectors.toList());
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

        // Map SchoolClass entity to DTO
        SchoolClassResponse schoolClassResponse = null;
        if (student.getSchoolClass() != null) {
            SchoolClass sc = student.getSchoolClass();
            schoolClassResponse = SchoolClassResponse.builder()
                    .id(sc.getId())
                    .code(sc.getClassIdentifier())
                    .name(sc.getName())
                    .description(sc.getDescription())
                    .createdAt(sc.getCreatedAt())
                    .updatedAt(sc.getUpdatedAt())
                    .build();
        }

        // Map Section entity to DTO
        SectionResponse sectionResponse = null;
        if (student.getSection() != null) {
            Section s = student.getSection();
            sectionResponse = SectionResponse.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .schoolClassId(s.getSchoolClass().getId())
                    .build();
        }

        // Get parents from JPA relationships (not from string fields)
        ParentInfo fatherInfo = null;
        ParentInfo motherInfo = null;
        ParentInfo guardianInfo = null;

        // Find father from parents collection
        if (student.getParents() != null && !student.getParents().isEmpty()) {
            // Try to identify father by parent type
            Optional<Parent> father = student.getParents().stream()
                    .filter(p -> p.getParentType() != null &&
                            (p.getParentType() == Parent.ParentType.FATHER ||
                                    p.getRelationshipToStudent() != null &&
                                            p.getRelationshipToStudent().equalsIgnoreCase("FATHER")))
                    .findFirst();

            if (father.isPresent()) {
                Parent f = father.get();
                fatherInfo = ParentInfo.builder()
                        .Id(f.getId())
                        .name(f.getFullName())
                        .occupation(f.getOccupation())
                        .phone(f.getPhone())
                        .email(f.getEmail())
                        .build();
            }

            // Find mother from parents collection
            Optional<Parent> mother = student.getParents().stream()
                    .filter(p -> p.getParentType() != null &&
                            (p.getParentType() == Parent.ParentType.MOTHER ||
                                    p.getRelationshipToStudent() != null &&
                                            p.getRelationshipToStudent().equalsIgnoreCase("MOTHER")))
                    .findFirst();

            if (mother.isPresent()) {
                Parent m = mother.get();
                motherInfo = ParentInfo.builder()
                        .Id(m.getId())
                        .name(m.getFullName())
                        .occupation(m.getOccupation())
                        .phone(m.getPhone())
                        .email(m.getEmail())
                        .build();
            }
        }

        // Get guardians from guardians collection
        if (student.getGuardians() != null && !student.getGuardians().isEmpty()) {
            Optional<Parent> guardian = student.getGuardians().stream().findFirst();
            if (guardian.isPresent()) {
                Parent g = guardian.get();
                guardianInfo = ParentInfo.builder()
                        .Id(g.getId())
                        .name(g.getFullName())
                        .phone(g.getPhone())
                        .email(g.getEmail())
                        .build();
            }
        }

        // Fallback to string fields if relationships don't exist
        if (fatherInfo == null && student.getFatherName() != null) {
            fatherInfo = ParentInfo.builder()
                    .name(student.getFatherName())
                    .occupation(student.getFatherOccupation())
                    .phone(student.getFatherPhone())
                    .email(student.getFatherEmail())
                    .build();
        }

        if (motherInfo == null && student.getMotherName() != null) {
            motherInfo = ParentInfo.builder()
                    .name(student.getMotherName())
                    .occupation(student.getMotherOccupation())
                    .phone(student.getMotherPhone())
                    .email(student.getMotherEmail())
                    .build();
        }

        if (guardianInfo == null && student.getGuardianName() != null) {
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

        // Build StudentResponse
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
                .schoolClass(schoolClassResponse)   // mapped DTO
                .section(sectionResponse)           // mapped DTO
                .admissionDate(student.getAdmissionDate())
                .status(student.getStatus().name())
                .photoUrl(student.getPhotoUrl())
                .fatherInfo(fatherInfo)
                .motherInfo(motherInfo)
                .guardianInfo(guardianInfo)
                .emergencyContact(emergencyContact)
                .build();
    }

    private ParentRequest toParentRequest(ParentInfo info, String parentType) {
        if (info == null || info.getName() == null) {
            return null; // Skip null parent info
        }
        
        String[] nameParts = info.getName().trim().split(" ", 2);

        String firstname = nameParts.length > 0 ? nameParts[0] : "";
        String lastname = nameParts.length > 1 ? nameParts[1] : "";
        String middlename = null; // ParentRequest doesn't have middlename field

        ParentRequest req = new ParentRequest();
        req.setFirstname(firstname);
        req.setLastname(lastname);
        req.setMidlename(middlename);
        req.setEmail(info.getEmail());
        req.setPhone(info.getPhone());
        req.setParentType(parentType);
        return req;
    }

    private void linkStudentToParent(Student student, String parentId, String relationshipType) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        // Find the parent
        Parent parent = parentRepository.findById(UUID.fromString(parentId))
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));
        
        // Verify tenant access
        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }
        
        // Link based on relationship type
        if ("GUARDIAN".equals(relationshipType)) {
            // Add to guardians relationship
            student.getGuardians().add(parent);
            parent.getWards().add(student);
            log.info("Linked student {} to guardian {}", student.getId(), parent.getId());
        } else {
            // Add to parents relationship (FATHER, MOTHER, etc.)
            student.getParents().add(parent);
            parent.getChildren().add(student);
            log.info("Linked student {} to parent {}", student.getId(), parent.getId());
        }
        
        // Save both sides of the relationship
        studentRepository.save(student);
        parentRepository.save(parent);
    }
}
