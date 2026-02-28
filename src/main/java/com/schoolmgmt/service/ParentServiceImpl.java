package com.schoolmgmt.service;

import com.schoolmgmt.dto.common.UserRequest;
import com.schoolmgmt.dto.request.ParentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ParentAlreadyExistsException;
import com.schoolmgmt.exception.ParentDeletionException;
import com.schoolmgmt.exception.ParentNotFoundException;
import com.schoolmgmt.model.Parent;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.ParentRepository;
import com.schoolmgmt.util.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.schoolmgmt.model.Student;


@Service
@RequiredArgsConstructor
@Slf4j
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final UserService userService;

    /**
     * Create a new parent + auto-create PARENT user account.
     */
    @Override
    @Transactional
    public ParentResponse createParent(ParentRequest request) {

        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Creating parent for tenant={} email={}", tenantId, request.getEmail());

        // Check if parent already exists by email
        Optional<Parent> existingParent = parentRepository.findByEmailAndTenantId(request.getEmail(), tenantId);
        
        if (existingParent.isPresent()) {
            log.info("Parent already exists with email: {}, returning existing parent", request.getEmail());
            Parent existing = existingParent.get();
            ParentResponse response = new ParentResponse();
            response.setParentId(existing.getId());
            response.setFirstname(existing.getFirstName());
            response.setLastname(existing.getLastName());
            response.setMiddlename(existing.getMiddleName());
            response.setEmail(existing.getEmail());
            response.setPhone(existing.getPhone());
            response.setUserId(existing.getUser() != null ? existing.getUser().getId().toString() : null);
            response.setStudentIds(existing.getAllStudents().stream().map(s -> s.getId()).collect(java.util.stream.Collectors.toList()));
            return response;
        }

        // Note: Parents can have multiple children, so we allow duplicate emails
        // The same parent can be linked to multiple students

        // Prepare user creation request
        UserRequest userReq = new UserRequest();
        userReq.setFirstName(request.getFirstname());
        userReq.setMiddleName(request.getMidlename());
        userReq.setLastName(request.getLastname());
        userReq.setEmail(request.getEmail());
        userReq.setPhone(request.getPhone());
//        userReq.setAvatarUrl(request.getPhotoUrl());
        userReq.setReferenceId(null);
        userReq.setReferenceType(null);

        // Create user account with role=PARENT
        User savedUser = userService.createUser("PARENT", userReq, tenantId);
        log.info("Parent user account created userId={}", savedUser.getUserId());

        // Create parent entity
        Parent parent = Parent.builder()
                .firstName(request.getFirstname())
                .middleName(request.getMidlename())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .parentType(Parent.ParentType.valueOf(request.getParentType().toUpperCase()))
//                .gender(request.getGender())
//                .parentType(request.getParentType())
//                .address(request.getAddress())
//                .city(request.getCity())
//                .state(request.getState())
//                .country(request.getCountry())
//                .postalCode(request.getPostalCode())
//                .occupation(request.getOccupation())
//                .employer(request.getEmployer())
//                .aadharNumber(request.getAadharNumber())
//                .panNumber(request.getPanNumber())
//                .voterId(request.getVoterId())
//                .relationshipToStudent(request.getRelationshipToStudent())
//                .isPrimaryContact(request.getIsPrimaryContact())
//                .isEmergencyContact(request.getIsEmergencyContact())
//                .canPickupChild(request.getCanPickupChild())
//                .preferredLanguage(request.getPreferredLanguage())
//                .receiveSms(request.getReceiveSms())
//                .receiveEmail(request.getReceiveEmail())
//                .receiveAppNotifications(request.getReceiveAppNotifications())
//                .notes(request.getNotes())
//                .specialInstructions(request.getSpecialInstructions())
//                .photoUrl(request.getPhotoUrl())
//                .tenantId(tenantId)
                .user(savedUser)
                .build();

        Parent savedParent = parentRepository.save(parent);
        log.info("Parent saved parentId={}", savedParent.getId());

        return mapToResponse(savedParent);
    }

    /**
     * When creating student → auto-create parent if not found
     */

    @Override
    @Transactional
    public Parent getOrCreateParentForStudent(Student student, ParentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        // check if parent already exists (by email)
        Parent existing = parentRepository
                .findByEmailAndTenantId(request.getEmail(), tenantId)
                .orElse(null);

        if (existing != null) {
            log.info("Parent already exists. Linking student {} to parent {}",
                    student.getId(), existing.getId());

            // ADD student to parent relationship
            existing.getChildren().add(student);
            // OR existing.getWards().add(student); based on your logic

            return parentRepository.save(existing);
        }

        // Only create new user if parent doesn't exist
        log.info("Parent not found, creating new parent for email: {}", request.getEmail());
        
        try {
            // Prepare user creation request
            UserRequest userReq = new UserRequest();
            userReq.setFirstName(request.getFirstname());
            userReq.setMiddleName(request.getMidlename());
            userReq.setLastName(request.getLastname());
            userReq.setEmail(request.getEmail());
            userReq.setPhone(request.getPhone());
    //        userReq.setAvatarUrl(request.getPhotoUrl());
            userReq.setReferenceId(null);
            userReq.setReferenceType(null);

            // create new user account for parent
            User parentUser = userService.createUser("PARENT", userReq, tenantId);
            log.info("Created parent user account: {}", parentUser.getUserId());

        Parent parent = Parent.builder()
                .firstName(request.getFirstname())
                .middleName(request.getMidlename())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .parentType(Parent.ParentType.valueOf(request.getParentType().toUpperCase()))
//                .tenantId(tenantId)
                .user(parentUser)
                .build();

        parent.getChildren().add(student);  // link the student

        Parent saved = parentRepository.save(parent);

        log.info("Parent created and linked to student: {}", saved.getId());
        return saved;
        
        } catch (BusinessException e) {
            log.error("Business error while creating parent user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating parent user for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to create parent user account: " + e.getMessage(), e);
        }
    }
    /**
     * Update parent details (no user update here unless required later)
     */
    @Override
    @Transactional
    public ParentResponse updateParent(UUID parentId, ParentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Updating parent tenant={} parentId={}", tenantId, parentId);

        // FIXED: Use findByIdWithAllStudents to eagerly load BOTH collections
        Parent parent = parentRepository.findByIdWithAllStudents(parentId)
                .orElseThrow(() -> new ParentNotFoundException("Parent not found"));

        parent.setFirstName(request.getFirstname());
        parent.setMiddleName(request.getMidlename());
        parent.setLastName(request.getLastname());
        parent.setPhone(request.getPhone());
//        parent.setAddress(request.getAddress());
//        parent.setCity(request.getCity());
//        parent.setState(request.getState());
//        parent.setCountry(request.getCountry());
//        parent.setPostalCode(request.getPostalCode());
//        parent.setGender(request.getGender());
//        parent.setOccupation(request.getOccupation());
//        parent.setEmployer(request.getEmployer());

        parentRepository.save(parent);
        log.info("Parent updated successfully parentId={}", parentId);

        return mapToResponse(parent);
    }

    /**
     * Delete parent (only when no students linked)
     */
    @Override
    @Transactional
    public void deleteParent(UUID parentId) {

        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Deleting parent tenant={} parentId={}", tenantId, parentId);

        // FIXED: Use findByIdWithAllStudents to eagerly load BOTH collections
        Parent parent = parentRepository.findByIdWithAllStudents(parentId)
                .orElseThrow(() -> new ParentNotFoundException("Parent not found"));

        if (!parent.getAllStudents().isEmpty()) {
            throw new ParentDeletionException("Parent cannot be deleted. Linked students exist.");
        }

        parentRepository.delete(parent);

        log.info("Parent deleted successfully parentId={}", parentId);
    }

    @Override
    public ParentResponse getParent(UUID parentId) {
        // FIXED: Use findByIdWithAllStudents to eagerly load BOTH collections
        Parent parent = parentRepository.findByIdWithAllStudents(parentId)
                .orElseThrow(() -> new ParentNotFoundException("Parent not found"));

        return mapToResponse(parent);
    }

    @Override
    public List<ParentResponse> getAllParents() {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Fetching all parents for tenant={}", tenantId);

        return parentRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Manual conversion: Parent → ParentResponse
     */
    private ParentResponse mapToResponse(Parent parent) {

        ParentResponse response = new ParentResponse();

        response.setParentId(parent.getId());
        response.setFirstname(parent.getFirstName());
        response.setMiddlename(parent.getMiddleName());
        response.setLastname(parent.getLastName());
        response.setEmail(parent.getEmail());
        response.setPhone(parent.getPhone());
        response.setUserId(parent.getUser().getUserId());

        // FIXED: Now this will work because BOTH collections are loaded
        response.setStudentIds(
                parent.getAllStudents().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList())
        );

        return response;
    }
}