package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TeacherCreationRequest;
import com.schoolmgmt.dto.request.TeacherUpdateRequest;
import com.schoolmgmt.dto.request.TeacherAssignmentRequest;
import com.schoolmgmt.dto.response.TeacherResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
<<<<<<< Updated upstream
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
>>>>>>> Stashed changes
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final UserService userService;
    private final UserRepository userRepository;



    @Transactional
    public TeacherResponse createTeacher(TeacherCreationRequest request) {
        try {
            // 1️⃣ Get tenant from context
            String tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                log.error("Tenant ID not found in context while creating teacher");
                throw new IllegalStateException("Cannot create teacher without tenant context");
            }
            log.info("Starting teacher creation for tenant: {}", tenantId);

            // 2️⃣ Create User account
            User savedUser = userService.createUser("TEACHER", request.getUserRequest(), tenantId);
            log.info("Teacher user created successfully: {} (userId: {}) for tenant: {}",
                    savedUser.getUsername(), savedUser.getUserId(), tenantId);

            // 3️⃣ Create Teacher entity with employeeId = userId
            Teacher teacher = Teacher.builder()
                    .employeeId(savedUser.getUserId()) // match userId
                    .firstName(request.getUserRequest().getFirstName())
                    .lastName(request.getUserRequest().getLastName())
                    .email(request.getUserRequest().getEmail())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .phone(request.getUserRequest().getPhone())
                    .address(request.getAddress())
                    .designation(request.getDesignation())
                    .joiningDate(request.getJoiningDate())
                    .status(Teacher.TeacherStatus.ACTIVE)
                    .user(savedUser) // link User
                    .build();

            // 4️⃣ Save Teacher
            Teacher savedTeacher = teacherRepository.save(teacher);
            log.info("Teacher profile created successfully: {} (Employee/User ID: {}) for tenant: {}",
                    savedTeacher.getFullName(), savedTeacher.getEmployeeId(), tenantId);

            return toTeacherResponse(savedTeacher);

        } catch (BusinessException be) {
            log.warn("Business exception while creating teacher: {}", be.getMessage());
            throw be;
        } catch (IllegalStateException ise) {
            log.error("Illegal state while creating teacher: {}", ise.getMessage());
            throw ise;
        } catch (Exception e) {
            log.error("Unexpected error while creating teacher for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating teacher", e);
        }
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

<<<<<<< Updated upstream
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
=======
    @Transactional
    public TeacherResponse getTeacherById(UUID teacherId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching teacher {} for tenant: {}", teacherId, tenantId);

            Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            log.debug("Teacher found: {} ({})", teacher.getFullName(), teacher.getEmployeeId());
            return toTeacherResponse(teacher);

        } catch (ResourceNotFoundException rnfe) {
            log.warn("Teacher not found: {}", teacherId);
            throw rnfe;
        } catch (Exception e) {
            log.error("Error fetching teacher {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching teacher", e);
        }
    }

    @Transactional
    public Page<TeacherResponse> getAllTeachers(String status, String department, Pageable pageable) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching teachers for tenant: {} with filters - status: {}, department: {}", 
                    tenantId, status, department);

            Page<Teacher> teachers;

            // Simple filtering without deprecated Specification
            if (status != null && !status.isEmpty() && department != null && !department.isEmpty()) {
                // Filter by both status and department
                Teacher.TeacherStatus statusEnum;
                try {
                    statusEnum = Teacher.TeacherStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid teacher status filter: {}", status);
                    statusEnum = null;
                }
                if (statusEnum != null) {
                    teachers = teacherRepository.findByStatusAndDepartmentAndTenantId(statusEnum, department, tenantId, pageable);
                } else {
                    teachers = teacherRepository.findByDepartmentAndTenantId(department, tenantId, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                // Filter by status only
                try {
                    Teacher.TeacherStatus statusEnum = Teacher.TeacherStatus.valueOf(status.toUpperCase());
                    teachers = teacherRepository.findByStatusAndTenantId(statusEnum, tenantId, pageable);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid teacher status filter: {}", status);
                    teachers = teacherRepository.findByTenantId(tenantId, pageable);
                }
            } else if (department != null && !department.isEmpty()) {
                // Filter by department only
                teachers = teacherRepository.findByDepartmentAndTenantId(department, tenantId, pageable);
            } else {
                // No filters - get all teachers for tenant
                teachers = teacherRepository.findByTenantId(tenantId, pageable);
            }

            log.debug("Found {} teachers for tenant: {}", teachers.getTotalElements(), tenantId);
            return teachers.map(this::toTeacherResponse);

        } catch (Exception e) {
            log.error("Error fetching teachers for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching teachers", e);
        }
    }

    @Transactional
    public TeacherResponse updateTeacher(UUID teacherId, TeacherUpdateRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Updating teacher {} for tenant: {}", teacherId, tenantId);

            Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            // Update fields
            if (request.getFirstName() != null) {
                teacher.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                teacher.setLastName(request.getLastName());
            }
            if (request.getEmail() != null) {
                teacher.setEmail(request.getEmail());
            }
            if (request.getPhone() != null) {
                teacher.setPhone(request.getPhone());
            }
            if (request.getAddress() != null) {
                teacher.setAddress(request.getAddress());
            }
            if (request.getDesignation() != null) {
                teacher.setDesignation(request.getDesignation());
            }
            if (request.getDepartment() != null) {
                teacher.setDepartment(request.getDepartment());
            }
            if (request.getStatus() != null) {
                teacher.setStatus(Teacher.TeacherStatus.valueOf(request.getStatus().toUpperCase()));
            }

            Teacher updatedTeacher = teacherRepository.save(teacher);
            log.info("Teacher updated successfully: {} ({})", updatedTeacher.getFullName(), updatedTeacher.getEmployeeId());

            return toTeacherResponse(updatedTeacher);

        } catch (ResourceNotFoundException rnfe) {
            log.warn("Teacher not found for update: {}", teacherId);
            throw rnfe;
        } catch (IllegalArgumentException iae) {
            log.warn("Invalid status value for teacher update: {}", request.getStatus());
            throw new BusinessException("Invalid teacher status: " + request.getStatus());
        } catch (Exception e) {
            log.error("Error updating teacher {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while updating teacher", e);
        }
    }

    @Transactional
    public void deleteTeacher(UUID teacherId) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Deleting teacher {} for tenant: {}", teacherId, tenantId);

            Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

            // Remove all assignments first (cascade delete)
            int deletedAssignments = teacherClassRepository.deleteByTeacherIdAndTenantId(teacherId, tenantId);
            log.info("Deleted {} assignments for teacher: {}", deletedAssignments, teacherId);

            // Remove section class teacher assignments
            sectionRepository.removeTeacherFromAllSections(teacherId, tenantId);
            log.info("Removed teacher {} from all section class teacher roles", teacherId);

            // Delete user account
            if (teacher.getUser() != null) {
                userRepository.delete(teacher.getUser());
                log.info("Deleted user account for teacher: {}", teacher.getUser().getUsername());
            }

            // Delete teacher
            teacherRepository.delete(teacher);
            log.info("Teacher deleted successfully: {} ({})", teacher.getFullName(), teacher.getEmployeeId());

        } catch (ResourceNotFoundException rnfe) {
            log.warn("Teacher not found for deletion: {}", teacherId);
            throw rnfe;
        } catch (Exception e) {
            log.error("Error deleting teacher {}: {}", teacherId, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while deleting teacher", e);
        }
    }

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
                .employeeType(teacher.getEmployeeType() != null ? teacher.getEmployeeType().name() : null)
                .department(teacher.getDepartment())
                .designation(teacher.getDesignation())
                .subjects(teacher.getSubjects())
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
>>>>>>> Stashed changes
    }
}