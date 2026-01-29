package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateParentRequest;
import com.schoolmgmt.dto.request.ParentFilterRequest;
import com.schoolmgmt.dto.request.UpdateParentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Parent;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.ParentRepository;
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
import java.util.stream.Collectors;

/**
 * Service for parent/guardian management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new parent/guardian
     */
    public ParentResponse createParent(CreateParentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Validate email uniqueness within tenant
        if (parentRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }

        // Validate phone uniqueness within tenant
        if (parentRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
            throw new BusinessException("Phone number already exists: " + request.getPhone());
        }

        // Create parent entity
        Parent parent = Parent.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .parentType(Parent.ParentType.valueOf(request.getParentType()))
                .gender(request.getGender() != null ? Parent.Gender.valueOf(request.getGender()) : null)
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .workPhone(request.getWorkPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .occupation(request.getOccupation())
                .employer(request.getEmployer())
                .workAddress(request.getWorkAddress())
                .annualIncome(request.getAnnualIncome())
                .educationLevel(request.getEducationLevel())
                .aadharNumber(request.getAadharNumber())
                .relationshipToStudent(request.getRelationshipToStudent())
                .isPrimaryContact(request.getIsPrimaryContact())
                .isEmergencyContact(request.getIsEmergencyContact())
                .canPickupChild(request.getCanPickupChild())
                .preferredLanguage(request.getPreferredLanguage())
                .receiveSms(request.getReceiveSms())
                .receiveEmail(request.getReceiveEmail())
                .receiveAppNotifications(request.getReceiveAppNotifications())
                .portalAccessEnabled(request.getPortalAccessEnabled())
                .photoUrl(request.getPhotoUrl())
                .notes(request.getNotes())
                .specialInstructions(request.getSpecialInstructions())
                .status(Parent.ParentStatus.ACTIVE)
                .build();

        parent.setTenantId(tenantId);

        // Save parent first
        Parent savedParent = parentRepository.save(parent);
        log.info("Parent created: {} - {} in tenant: {}",
                savedParent.getId(), savedParent.getFullName(), tenantId);

        // Create user account if requested
        if (request.isCreateUserAccount() && request.getEmail() != null) {
            try {
                User user = createUserForParent(savedParent);
                savedParent.setUser(user);
                savedParent = parentRepository.save(savedParent);
                log.info("User account created for parent: {}", savedParent.getFullName());
            } catch (Exception e) {
                log.error("Failed to create user account for parent: {}", savedParent.getFullName(), e);
                throw new BusinessException("Parent created but failed to create user account: " + e.getMessage());
            }
        }

        return toParentResponse(savedParent);
    }

    /**
     * Update parent information
     */
    public ParentResponse updateParent(UUID parentId, UpdateParentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));

        // Verify tenant access
        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }

        // Update fields if provided
        if (request.getFirstName() != null) {
            parent.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            parent.setMiddleName(request.getMiddleName());
        }
        if (request.getLastName() != null) {
            parent.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            // Validate email uniqueness if changed
            if (!request.getEmail().equals(parent.getEmail())
                    && parentRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
                throw new BusinessException("Email already exists: " + request.getEmail());
            }
            parent.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            // Validate phone uniqueness if changed
            if (!request.getPhone().equals(parent.getPhone())
                    && parentRepository.existsByPhoneAndTenantId(request.getPhone(), tenantId)) {
                throw new BusinessException("Phone number already exists: " + request.getPhone());
            }
            parent.setPhone(request.getPhone());
        }
        if (request.getAlternatePhone() != null) {
            parent.setAlternatePhone(request.getAlternatePhone());
        }
        if (request.getWorkPhone() != null) {
            parent.setWorkPhone(request.getWorkPhone());
        }
        if (request.getAddress() != null) {
            parent.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            parent.setCity(request.getCity());
        }
        if (request.getState() != null) {
            parent.setState(request.getState());
        }
        if (request.getCountry() != null) {
            parent.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            parent.setPostalCode(request.getPostalCode());
        }
        if (request.getOccupation() != null) {
            parent.setOccupation(request.getOccupation());
        }
        if (request.getEmployer() != null) {
            parent.setEmployer(request.getEmployer());
        }
        if (request.getWorkAddress() != null) {
            parent.setWorkAddress(request.getWorkAddress());
        }
        if (request.getAnnualIncome() != null) {
            parent.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getEducationLevel() != null) {
            parent.setEducationLevel(request.getEducationLevel());
        }
        if (request.getRelationshipToStudent() != null) {
            parent.setRelationshipToStudent(request.getRelationshipToStudent());
        }
        if (request.getIsPrimaryContact() != null) {
            parent.setIsPrimaryContact(request.getIsPrimaryContact());
        }
        if (request.getIsEmergencyContact() != null) {
            parent.setIsEmergencyContact(request.getIsEmergencyContact());
        }
        if (request.getCanPickupChild() != null) {
            parent.setCanPickupChild(request.getCanPickupChild());
        }
        if (request.getPreferredLanguage() != null) {
            parent.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getReceiveSms() != null) {
            parent.setReceiveSms(request.getReceiveSms());
        }
        if (request.getReceiveEmail() != null) {
            parent.setReceiveEmail(request.getReceiveEmail());
        }
        if (request.getReceiveAppNotifications() != null) {
            parent.setReceiveAppNotifications(request.getReceiveAppNotifications());
        }
        if (request.getPortalAccessEnabled() != null) {
            parent.setPortalAccessEnabled(request.getPortalAccessEnabled());
        }
        if (request.getPhotoUrl() != null) {
            parent.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getNotes() != null) {
            parent.setNotes(request.getNotes());
        }
        if (request.getSpecialInstructions() != null) {
            parent.setSpecialInstructions(request.getSpecialInstructions());
        }

        Parent updatedParent = parentRepository.save(parent);
        log.info("Parent updated: {} - {}", updatedParent.getId(), updatedParent.getFullName());

        return toParentResponse(updatedParent);
    }

    /**
     * Get parent by ID
     */
    @Transactional(readOnly = true)
    public ParentResponse getParentById(UUID parentId) {
        String tenantId = TenantContext.requireCurrentTenant();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));

        // Verify tenant access
        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }

        return toParentResponse(parent);
    }

    /**
     * Get all parents with filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<ParentResponse> getAllParents(ParentFilterRequest filter, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();

        Specification<Parent> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)
        );

        // Add filters
        if (filter != null) {
            if (filter.getParentType() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("parentType"), Parent.ParentType.valueOf(filter.getParentType())));
            }
            if (filter.getStatus() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), Parent.ParentStatus.valueOf(filter.getStatus())));
            }
            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                spec = spec.and((root, query, cb) -> cb.or(
                        cb.like(cb.lower(root.get("firstName")), searchTerm),
                        cb.like(cb.lower(root.get("lastName")), searchTerm),
                        cb.like(cb.lower(root.get("email")), searchTerm),
                        cb.like(cb.lower(root.get("phone")), searchTerm)
                ));
            }
        }

        Page<Parent> parents = parentRepository.findAll(spec, pageable);
        return parents.map(this::toParentResponse);
    }

    /**
     * Update parent status
     */
    public void updateParentStatus(UUID parentId, String status) {
        String tenantId = TenantContext.requireCurrentTenant();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));

        // Verify tenant access
        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }

        Parent.ParentStatus parentStatus = Parent.ParentStatus.valueOf(status.toUpperCase());
        parentRepository.updateStatus(parentId, parentStatus);

        log.info("Parent status updated: {} to {}", parent.getFullName(), status);
    }

    /**
     * Delete parent (soft delete)
     */
    public void deleteParent(UUID parentId) {
        String tenantId = TenantContext.requireCurrentTenant();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));

        // Verify tenant access
        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }

        parent.softDelete(tenantId);
        parentRepository.save(parent);

        log.info("Parent soft deleted: {}", parent.getFullName());
    }

    /**
     * Get parents by student ID
     */
    @Transactional(readOnly = true)
    public List<ParentResponse> getParentsByStudentId(UUID studentId) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Verify student exists and belongs to tenant
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        List<Parent> parents = parentRepository.findAllByStudentId(studentId);
        return parents.stream()
                .map(this::toParentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Link parent to student (adds to children relationship)
     */
    public ParentResponse linkParentToStudent(UUID parentId, UUID studentId) {
        String tenantId = TenantContext.requireCurrentTenant();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));

        if (!parent.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Parent", "id", parentId);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (!student.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        // Link based on parent type
        if (parent.getParentType() == Parent.ParentType.GUARDIAN) {
            student.getGuardians().add(parent);
        } else {
            student.getParents().add(parent);
        }

        studentRepository.save(student);

        log.info("Parent {} linked to student {}", parent.getFullName(), student.getFullName());

        return toParentResponse(parent);
    }

    /**
     * Create user account for parent
     */
    private User createUserForParent(Parent parent) {
        String defaultPassword = "Parent@" + UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .email(parent.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .firstName(parent.getFirstName())
                .lastName(parent.getLastName())
                .role(User.UserRole.PARENT)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .isActive(true)
                .referenceId(parent.getId().toString())
                .referenceType("PARENT")
                .build();

        user.setTenantId(parent.getTenantId());
        User savedUser = userRepository.save(user);

        return savedUser;
    }

    /**
     * Convert Parent entity to ParentResponse DTO
     */
    private ParentResponse toParentResponse(Parent parent) {
        // Build linked students list
        List<ParentResponse.LinkedStudentInfo> linkedStudents = new ArrayList<>();

        for (Student child : parent.getChildren()) {
            linkedStudents.add(ParentResponse.LinkedStudentInfo.builder()
                    .id(child.getId().toString())
                    .fullName(child.getFullName())
                    .rollNumber(child.getRollNumber())
                    .currentClassId(child.getCurrentClassId())
                    .relationship("child")
                    .build());
        }

        for (Student ward : parent.getWards()) {
            linkedStudents.add(ParentResponse.LinkedStudentInfo.builder()
                    .id(ward.getId().toString())
                    .fullName(ward.getFullName())
                    .rollNumber(ward.getRollNumber())
                    .currentClassId(ward.getCurrentClassId())
                    .relationship("ward")
                    .build());
        }

        return ParentResponse.builder()
                .id(parent.getId().toString())
                .firstName(parent.getFirstName())
                .middleName(parent.getMiddleName())
                .lastName(parent.getLastName())
                .fullName(parent.getFullName())
                .parentType(parent.getParentType().name())
                .gender(parent.getGender() != null ? parent.getGender().name() : null)
                .dateOfBirth(parent.getDateOfBirth())
                .email(parent.getEmail())
                .phone(parent.getPhone())
                .alternatePhone(parent.getAlternatePhone())
                .workPhone(parent.getWorkPhone())
                .address(parent.getAddress())
                .city(parent.getCity())
                .state(parent.getState())
                .country(parent.getCountry())
                .postalCode(parent.getPostalCode())
                .occupation(parent.getOccupation())
                .employer(parent.getEmployer())
                .workAddress(parent.getWorkAddress())
                .annualIncome(parent.getAnnualIncome())
                .educationLevel(parent.getEducationLevel())
                .relationshipToStudent(parent.getRelationshipToStudent())
                .isPrimaryContact(parent.getIsPrimaryContact())
                .isEmergencyContact(parent.getIsEmergencyContact())
                .canPickupChild(parent.getCanPickupChild())
                .preferredLanguage(parent.getPreferredLanguage())
                .receiveSms(parent.getReceiveSms())
                .receiveEmail(parent.getReceiveEmail())
                .receiveAppNotifications(parent.getReceiveAppNotifications())
                .status(parent.getStatus().name())
                .portalAccessEnabled(parent.getPortalAccessEnabled())
                .photoUrl(parent.getPhotoUrl())
                .notes(parent.getNotes())
                .specialInstructions(parent.getSpecialInstructions())
                .linkedStudents(linkedStudents)
                .build();
    }
}
