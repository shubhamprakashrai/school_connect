package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.AdminRequest;
import com.schoolmgmt.dto.request.AdminUpdateRequest;
import com.schoolmgmt.dto.response.AdminResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.InternalServiceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Admin;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.AdminRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service class for Admin entity operations.
 * Handles admin profile management with proper multi-tenant support and validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Create a new admin with user account.
     * This method creates the user account first via UserService, then creates the admin profile.
     *
     * @param request Admin creation request
     * @return AdminResponse with created admin details
     */
    @Transactional
    public AdminResponse createAdmin(AdminRequest request) {
        String tenantId;
        try {
            // 1. Get tenant from context
            tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                log.error("Tenant ID not found in context while creating admin");
                throw new IllegalStateException("Cannot create admin without tenant context");
            }
            log.info("========== STARTING ADMIN CREATION ==========");
            log.info("Creating admin for tenant: {}, employeeId: {}", tenantId, request.getEmployeeId());

            // 2. Validate employee ID uniqueness within tenant (including deleted records)
            if (adminRepository.existsByEmployeeIdAndTenantIdIncludingDeleted(request.getEmployeeId(), tenantId)) {
                log.warn("Employee ID already exists in tenant (including deleted records): {}", request.getEmployeeId());
                throw new BusinessException("Employee ID already exists in this school: " + request.getEmployeeId() + ". Please use a different employee ID.");
            }

            // 3. Validate phone format (10 digits) - phone comes from UserRequest
            if (request.getUserRequest().getPhone() != null) {
                String cleanPhone = request.getUserRequest().getPhone().trim();
                if (!cleanPhone.matches("^[0-9]{10}$")) {
                    log.warn("Invalid phone format: {}", cleanPhone);
                    throw new BusinessException("Phone number must be exactly 10 digits: " + cleanPhone);
                }
                if (cleanPhone.length() != 10) {
                    log.warn("Invalid phone length: {} digits", cleanPhone.length());
                    throw new BusinessException("Phone number must be exactly 10 digits, provided: " + cleanPhone.length() + " digits");
                }
            }

            // 4. Validate alternate phone format if provided
            if (request.getAlternatePhone() != null) {
                String cleanAltPhone = request.getAlternatePhone().trim();
                if (!cleanAltPhone.matches("^[0-9]{10}$")) {
                    log.warn("Invalid alternate phone format: {}", cleanAltPhone);
                    throw new BusinessException("Alternate phone number must be exactly 10 digits: " + cleanAltPhone);
                }
            }

            // 5. Create User account with ADMIN role
            Set<User.UserRole> roles = new HashSet<>();
            roles.add(User.UserRole.ADMIN);
            if (request.getAdditionalRoles() != null) {
                roles.addAll(request.getAdditionalRoles());
            }
            log.info("Creating user account with roles: {} for tenant: {}", roles, tenantId);
            User savedUser = userService.createUserWithRoles(roles, request.getUserRequest(), tenantId);
            log.info("Admin user created successfully: {} (userId: {}) for tenant: {}",
                    savedUser.getUsername(), savedUser.getUserId(), tenantId);

            // 6. Create Admin entity
            Admin admin = Admin.builder()
                    .employeeId(request.getEmployeeId())
                    .user(savedUser)
                    .firstName(request.getUserRequest().getFirstName())
                    .lastName(request.getUserRequest().getLastName())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .phone(request.getUserRequest().getPhone())
                    .alternatePhone(request.getAlternatePhone())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .postalCode(request.getPostalCode())
                    .country(request.getCountry())
                    .department(request.getDepartment())
                    .designation(request.getDesignation())
                    .joinDate(request.getJoinDate())
                    .salary(request.getSalary())
                    .employmentType(request.getEmploymentType())
                    .status(Admin.AdminStatus.ACTIVE)
                    .permissions(request.getPermissions() != null ? request.getPermissions() : new HashSet<>())
                    .profileImageUrl(request.getProfileImageUrl())
                    .notes(request.getNotes())
                    .build();

            // 7. Save Admin
            Admin savedAdmin = adminRepository.save(admin);
            // Initialize lazy-loaded collections before returning
            savedAdmin.getPermissions().size();
            log.info("Admin profile created successfully: {} (Employee ID: {}) for tenant: {}",
                    savedAdmin.getFirstName() + " " + savedAdmin.getLastName(), savedAdmin.getEmployeeId(), tenantId);
            log.info("========== ADMIN CREATION COMPLETED SUCCESSFULLY ==========");

            return toAdminResponse(savedAdmin);

        } catch (BusinessException be) {
            log.warn("Business exception while creating admin: {}", be.getMessage());
            throw be;
        } catch (IllegalStateException ise) {
            log.error("Illegal state while creating admin: {}", ise.getMessage());
            throw ise;
        } catch (Exception e) {
            log.error("Unexpected error while creating admin for tenant {}: {}", 
                    TenantContext.getCurrentTenant(), e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating admin", e);
        }
    }

    /**
     * Get admin by ID.
     *
     * @param adminId Admin ID
     * @return AdminResponse with admin details
     */
    @Transactional(readOnly = true)
    public AdminResponse getAdminById(UUID adminId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Fetching admin by ID: {} for tenant: {}", adminId, tenantId);

        Admin admin = adminRepository.findByIdAndTenantId(adminId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Admin not found with ID: {} for tenant: {}", adminId, tenantId);
                    return new ResourceNotFoundException("Admin not found with ID: " + adminId);
                });

        // Initialize lazy-loaded collections
        admin.getPermissions().size();
        log.info("Admin found: {} (Employee ID: {})", admin.getFirstName() + " " + admin.getLastName(), admin.getEmployeeId());
        return toAdminResponse(admin);
    }

    /**
     * Get all admins with pagination and filtering.
     *
     * @param status Filter by status (optional)
     * @param department Filter by department (optional)
     * @param designation Filter by designation (optional)
     * @param pageable Pagination information
     * @return Page of AdminResponse
     */
    @Transactional(readOnly = true)
    public Page<AdminResponse> getAllAdmins(String status, String department, String designation, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Fetching admins for tenant: {} with filters - status: {}, department: {}, designation: {}",
                tenantId, status, department, designation);

        Page<Admin> admins;
        Admin.AdminStatus adminStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                adminStatus = Admin.AdminStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
                throw new BusinessException("Invalid status value: " + status);
            }
        }

        // Build query based on provided filters
        if (adminStatus != null && department != null && designation != null) {
            admins = adminRepository.findByStatusAndDepartmentAndDesignationAndTenantId(
                    adminStatus, department, designation, tenantId, pageable);
        } else if (adminStatus != null && department != null) {
            admins = adminRepository.findByStatusAndDepartmentAndTenantId(
                    adminStatus, department, tenantId, pageable);
        } else if (adminStatus != null && designation != null) {
            admins = adminRepository.findByStatusAndDesignationAndTenantId(
                    adminStatus, designation, tenantId, pageable);
        } else if (adminStatus != null) {
            admins = adminRepository.findByStatusAndTenantId(adminStatus, tenantId, pageable);
        } else if (department != null) {
            admins = adminRepository.findByDepartmentAndTenantId(department, tenantId, pageable);
        } else if (designation != null) {
            admins = adminRepository.findByDesignationAndTenantId(designation, tenantId, pageable);
        } else {
            admins = adminRepository.findByTenantId(tenantId, pageable);
        }

        log.info("Found {} admins for tenant: {}", admins.getTotalElements(), tenantId);
        return admins.map(admin -> {
            admin.getPermissions().size(); // Initialize lazy-loaded collection
            return toAdminResponse(admin);
        });
    }

    /**
     * Update admin information.
     *
     * @param adminId Admin ID
     * @param request Update request
     * @return Updated AdminResponse
     */
    @Transactional
    public AdminResponse updateAdmin(UUID adminId, AdminUpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("========== STARTING ADMIN UPDATE ==========");
        log.info("Updating admin ID: {} for tenant: {}", adminId, tenantId);

        try {
            // 1. Fetch existing admin
            Admin admin = adminRepository.findByIdAndTenantId(adminId, tenantId)
                    .orElseThrow(() -> {
                        log.warn("Admin not found with ID: {} for tenant: {}", adminId, tenantId);
                        return new ResourceNotFoundException("Admin not found with ID: " + adminId);
                    });

            // 2. Validate phone format if provided
            if (request.getPhone() != null) {
                String cleanPhone = request.getPhone().trim();
                if (!cleanPhone.matches("^[0-9]{10}$")) {
                    log.warn("Invalid phone format: {}", cleanPhone);
                    throw new BusinessException("Phone number must be exactly 10 digits: " + cleanPhone);
                }
            }

            // 3. Validate alternate phone format if provided
            if (request.getAlternatePhone() != null) {
                String cleanAltPhone = request.getAlternatePhone().trim();
                if (!cleanAltPhone.matches("^[0-9]{10}$")) {
                    log.warn("Invalid alternate phone format: {}", cleanAltPhone);
                    throw new BusinessException("Alternate phone number must be exactly 10 digits: " + cleanAltPhone);
                }
            }

            // 4. Update fields (only non-null fields)
            if (request.getFirstName() != null) {
                admin.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                admin.setLastName(request.getLastName());
            }
            if (request.getDateOfBirth() != null) {
                admin.setDateOfBirth(request.getDateOfBirth());
            }
            if (request.getGender() != null) {
                admin.setGender(request.getGender());
            }
            if (request.getPhone() != null) {
                admin.setPhone(request.getPhone());
            }
            if (request.getAlternatePhone() != null) {
                admin.setAlternatePhone(request.getAlternatePhone());
            }
            if (request.getAddress() != null) {
                admin.setAddress(request.getAddress());
            }
            if (request.getCity() != null) {
                admin.setCity(request.getCity());
            }
            if (request.getState() != null) {
                admin.setState(request.getState());
            }
            if (request.getPostalCode() != null) {
                admin.setPostalCode(request.getPostalCode());
            }
            if (request.getCountry() != null) {
                admin.setCountry(request.getCountry());
            }
            if (request.getDepartment() != null) {
                admin.setDepartment(request.getDepartment());
            }
            if (request.getDesignation() != null) {
                admin.setDesignation(request.getDesignation());
            }
            if (request.getSalary() != null) {
                admin.setSalary(request.getSalary());
            }
            if (request.getEmploymentType() != null) {
                admin.setEmploymentType(request.getEmploymentType());
            }
            if (request.getStatus() != null) {
                admin.setStatus(request.getStatus());
            }
            if (request.getPermissions() != null) {
                admin.setPermissions(request.getPermissions());
            }
            if (request.getProfileImageUrl() != null) {
                admin.setProfileImageUrl(request.getProfileImageUrl());
            }
            if (request.getNotes() != null) {
                admin.setNotes(request.getNotes());
            }

            // 5. Save updated admin
            Admin updatedAdmin = adminRepository.save(admin);
            // Initialize lazy-loaded collections
            updatedAdmin.getPermissions().size();
            log.info("Admin updated successfully: {} (Employee ID: {})", updatedAdmin.getFirstName() + " " + updatedAdmin.getLastName(), updatedAdmin.getEmployeeId());
            log.info("========== ADMIN UPDATE COMPLETED SUCCESSFULLY ==========");

            return toAdminResponse(updatedAdmin);

        } catch (ResourceNotFoundException rnf) {
            log.warn("Resource not found while updating admin: {}", rnf.getMessage());
            throw rnf;
        } catch (BusinessException be) {
            log.warn("Business exception while updating admin: {}", be.getMessage());
            throw be;
        } catch (Exception e) {
            log.error("Unexpected error while updating admin ID: {} for tenant: {}", adminId, tenantId, e);
            throw new InternalServiceException("Internal server error while updating admin", e);
        }
    }

    /**
     * Delete admin (soft delete).
     * This will mark both the admin profile and the user account as deleted.
     *
     * @param adminId Admin ID
     */
    @Transactional
    public void deleteAdmin(UUID adminId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("========== STARTING ADMIN DELETION ==========");
        log.info("Deleting admin ID: {} for tenant: {}", adminId, tenantId);

        try {
            // 1. Fetch admin
            Admin admin = adminRepository.findByIdAndTenantId(adminId, tenantId)
                    .orElseThrow(() -> {
                        log.warn("Admin not found with ID: {} for tenant: {}", adminId, tenantId);
                        return new ResourceNotFoundException("Admin not found with ID: " + adminId);
                    });

            log.info("Admin found: {} (Employee ID: {}, User: {})",
                    admin.getFirstName() + " " + admin.getLastName(),
                    admin.getEmployeeId(),
                    admin.getUser() != null ? admin.getUser().getEmail() : "NULL");

            // 2. Soft delete admin profile
            admin.setIsDeleted(true);
            admin.setDeletedAt(java.time.LocalDateTime.now());
            adminRepository.save(admin);
            log.info("Admin profile soft deleted: {} (Employee ID: {})", admin.getFirstName() + " " + admin.getLastName(), admin.getEmployeeId());

            // 3. Soft delete associated user account
            User userToDelete = admin.getUser();
            if (userToDelete == null) {
                // Fallback 1: Try JPQL query
                log.warn("User relationship is null, using fallback to find user ID by admin ID");
                Optional<UUID> userIdOptional = adminRepository.findUserIdByAdminId(adminId);
                if (userIdOptional.isPresent()) {
                    UUID userId = userIdOptional.get();
                    log.info("Found user ID: {} for admin ID: {} using JPQL", userId, adminId);
                    userToDelete = userRepository.findById(userId).orElse(null);
                } else {
                    // Fallback 2: Try native query
                    log.warn("JPQL query failed, trying native query");
                    userIdOptional = adminRepository.findUserIdByAdminIdNative(adminId);
                    if (userIdOptional.isPresent()) {
                        UUID userId = userIdOptional.get();
                        log.info("Found user ID: {} for admin ID: {} using native query", userId, adminId);
                        userToDelete = userRepository.findById(userId).orElse(null);
                    } else {
                        log.error("Cannot find user ID for admin ID: {} using any method", adminId);
                        throw new InternalServiceException("Cannot delete admin: user account not found");
                    }
                }
            }

            if (userToDelete != null) {
                log.info("Deleting user account: {} (ID: {})", userToDelete.getEmail(), userToDelete.getId());
                userToDelete.setIsDeleted(true);
                userToDelete.setDeletedAt(java.time.LocalDateTime.now());
                userRepository.save(userToDelete);
                log.info("Associated user account soft deleted: {}", userToDelete.getUsername());
            }

            log.info("========== ADMIN DELETION COMPLETED SUCCESSFULLY ==========");

        } catch (ResourceNotFoundException rnf) {
            log.warn("Resource not found while deleting admin: {}", rnf.getMessage());
            throw rnf;
        } catch (Exception e) {
            log.error("Unexpected error while deleting admin ID: {} for tenant: {}", adminId, tenantId, e);
            throw new InternalServiceException("Internal server error while deleting admin", e);
        }
    }

    /**
     * Convert Admin entity to AdminResponse DTO.
     *
     * @param admin Admin entity
     * @return AdminResponse DTO
     */
    private AdminResponse toAdminResponse(Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .userId(admin.getUser() != null ? admin.getUser().getId() : null)
                .employeeId(admin.getEmployeeId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .dateOfBirth(admin.getDateOfBirth())
                .gender(admin.getGender() != null ? admin.getGender().name() : null)
                .email(admin.getUser() != null ? admin.getUser().getEmail() : null)
                .phone(admin.getPhone())
                .alternatePhone(admin.getAlternatePhone())
                .address(admin.getAddress())
                .city(admin.getCity())
                .state(admin.getState())
                .postalCode(admin.getPostalCode())
                .country(admin.getCountry())
                .department(admin.getDepartment())
                .designation(admin.getDesignation())
                .joinDate(admin.getJoinDate())
                .salary(admin.getSalary())
                .employmentType(admin.getEmploymentType() != null ? admin.getEmploymentType().name() : null)
                .status(admin.getStatus() != null ? admin.getStatus().name() : null)
                .permissions(admin.getPermissions())
                .profileImageUrl(admin.getProfileImageUrl())
                .notes(admin.getNotes())
                .tenantId(admin.getTenantId())
                .createdAt(admin.getCreatedAt() != null ? admin.getCreatedAt().toLocalDate() : null)
                .updatedAt(admin.getUpdatedAt() != null ? admin.getUpdatedAt().toLocalDate() : null)
                .createdBy(admin.getCreatedBy())
                .updatedBy(admin.getUpdatedBy())
                .build();
    }
}
