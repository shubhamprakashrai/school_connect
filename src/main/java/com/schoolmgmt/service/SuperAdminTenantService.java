package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TenantFilterRequest;
import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Tenant;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.TenantIdFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for SuperAdmin tenant management operations.
 * Provides comprehensive tenant management capabilities for system administrators.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuperAdminTenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Create a new tenant as SuperAdmin
     */
    public TenantRegistrationResponse createTenant(TenantRegistrationRequest request) {
        log.info("SuperAdmin creating tenant: {}", request.getName());

        validateTenantRequest(request);

        // Extract initials from school name
        String initials = TenantIdFormatter.extractInitials(request.getName());
        int nextSequence = tenantRepository.getNextTenantSequence();
        String tenantIdentifier = TenantIdFormatter.format(initials, nextSequence);
        String schemaName = "school_" + request.getSubdomain().toLowerCase().replace("-", "_");

        Tenant tenant = Tenant.builder()
                .identifier(tenantIdentifier)
                .name(request.getName())
                .subdomain(request.getSubdomain())
                .schemaName(schemaName)
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .website(request.getWebsite())
                .status(Tenant.TenantStatus.ACTIVE) // SuperAdmin creates active tenants by default
                .subscriptionPlan(Tenant.SubscriptionPlan.valueOf(request.getSubscriptionPlan()))
                .createdBy("SUPER_ADMIN")
                .build();

        setDefaultLimits(tenant, request.getSubscriptionPlan());

        if (request.getConfiguration() != null) {
            tenant.setConfiguration(request.getConfiguration().toString());
        }

        Tenant savedTenant = tenantRepository.save(tenant);

        // Create admin user if requested
        if (request.getAdminUser() != null) {
            createTenantAdminUser(savedTenant, request.getAdminUser());
        }

        log.info("SuperAdmin successfully created tenant: {} with identifier: {}", 
                savedTenant.getName(), savedTenant.getIdentifier());

        return toTenantRegistrationResponse(savedTenant);
    }

    /**
     * Get all tenants with filtering and pagination
     */
    public Page<TenantResponse> getAllTenants(TenantFilterRequest filter, Pageable pageable) {
        Specification<Tenant> spec = buildTenantSpecification(filter);
        Page<Tenant> tenants = tenantRepository.findAll(spec, pageable);
        return tenants.map(this::toTenantResponse);
    }

    /**
     * Get tenant by ID
     */
    public TenantResponse getTenantById(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        return toTenantResponse(tenant);
    }

    /**
     * Update tenant information
     */
    public TenantResponse updateTenant(UUID tenantId, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        updateTenantFields(tenant, request);
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("SuperAdmin updated tenant: {}", tenantId);
        return toTenantResponse(updatedTenant);
    }

    /**
     * Activate a tenant
     */
    public TenantResponse activateTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setActivatedAt(LocalDateTime.now());
        tenant.setSuspendedAt(null);

        if (reason != null) {
            tenant.setConfiguration(updateConfigWithReason(tenant.getConfiguration(), "activation_reason", reason));
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("SuperAdmin activated tenant: {} - {}", tenantId, tenant.getName());

        return toTenantResponse(updatedTenant);
    }

    /**
     * Suspend a tenant
     */
    public TenantResponse suspendTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        tenant.setSuspendedAt(LocalDateTime.now());

        if (reason != null) {
            tenant.setConfiguration(updateConfigWithReason(tenant.getConfiguration(), "suspension_reason", reason));
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("SuperAdmin suspended tenant: {} - {} for reason: {}", tenantId, tenant.getName(), reason);

        return toTenantResponse(updatedTenant);
    }

    /**
     * Soft delete a tenant
     */
    public void deleteTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        tenant.setStatus(Tenant.TenantStatus.DELETED);
        tenant.setDeletedAt(LocalDateTime.now());

        if (reason != null) {
            tenant.setConfiguration(updateConfigWithReason(tenant.getConfiguration(), "deletion_reason", reason));
        }

        tenantRepository.save(tenant);
        log.info("SuperAdmin soft deleted tenant: {} - {} for reason: {}", tenantId, tenant.getName(), reason);
    }

    /**
     * Permanently delete a tenant
     */
    public void permanentlyDeleteTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        // Delete all associated users first
        List<User> tenantUsers = userRepository.findByTenantId(tenant.getIdentifier());
        userRepository.deleteAll(tenantUsers);

        // Delete the tenant
        tenantRepository.delete(tenant);
        log.warn("SuperAdmin permanently deleted tenant: {} - {}", tenantId, tenant.getName());
    }

    /**
     * Restore a deleted tenant
     */
    public TenantResponse restoreTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (tenant.getStatus() != Tenant.TenantStatus.DELETED) {
            throw new BusinessException("Tenant is not in deleted status");
        }

        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setDeletedAt(null);
        tenant.setActivatedAt(LocalDateTime.now());

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("SuperAdmin restored tenant: {} - {}", tenantId, tenant.getName());

        return toTenantResponse(updatedTenant);
    }

    /**
     * Update tenant subscription plan
     */
    public TenantResponse updateSubscriptionPlan(UUID tenantId, String subscriptionPlan, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Tenant.SubscriptionPlan oldPlan = tenant.getSubscriptionPlan();
        Tenant.SubscriptionPlan newPlan = Tenant.SubscriptionPlan.valueOf(subscriptionPlan);

        tenant.setSubscriptionPlan(newPlan);
        setDefaultLimits(tenant, subscriptionPlan);

        if (reason != null) {
            tenant.setConfiguration(updateConfigWithReason(tenant.getConfiguration(), 
                "subscription_change_reason", reason + " (from " + oldPlan + " to " + newPlan + ")"));
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("SuperAdmin updated subscription for tenant: {} from {} to {}", 
                tenantId, oldPlan, newPlan);

        return toTenantResponse(updatedTenant);
    }

    /**
     * Update tenant limits manually
     */
    public TenantResponse updateTenantLimits(UUID tenantId, Integer maxStudents, Integer maxTeachers, Integer maxStorageGb) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (maxStudents != null) {
            tenant.setMaxStudents(maxStudents);
        }
        if (maxTeachers != null) {
            tenant.setMaxTeachers(maxTeachers);
        }
        if (maxStorageGb != null) {
            tenant.setMaxStorageGb(maxStorageGb);
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("SuperAdmin updated limits for tenant: {} - Students: {}, Teachers: {}, Storage: {}GB", 
                tenantId, maxStudents, maxTeachers, maxStorageGb);

        return toTenantResponse(updatedTenant);
    }

    /**
     * Get global tenant statistics
     */
    public Map<String, Object> getGlobalTenantStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.countByStatus(Tenant.TenantStatus.ACTIVE);
        long suspendedTenants = tenantRepository.countByStatus(Tenant.TenantStatus.SUSPENDED);
        long pendingTenants = tenantRepository.countByStatus(Tenant.TenantStatus.PENDING);
        long deletedTenants = tenantRepository.countByStatus(Tenant.TenantStatus.DELETED);

        stats.put("totalTenants", totalTenants);
        stats.put("activeTenants", activeTenants);
        stats.put("suspendedTenants", suspendedTenants);
        stats.put("pendingTenants", pendingTenants);
        stats.put("deletedTenants", deletedTenants);

        // Statistics by subscription plan
        Map<String, Long> planStats = new HashMap<>();
        for (Tenant.SubscriptionPlan plan : Tenant.SubscriptionPlan.values()) {
            long count = tenantRepository.countBySubscriptionPlan(plan);
            if (count > 0) {
                planStats.put(plan.name(), count);
            }
        }
        stats.put("tenantsBySubscriptionPlan", planStats);

        // Recent registrations (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentRegistrations = tenantRepository.countByCreatedAtAfter(thirtyDaysAgo);
        stats.put("recentRegistrations", recentRegistrations);

        return stats;
    }

    /**
     * Get tenants by status
     */
    public List<TenantResponse> getTenantsByStatus(String status) {
        Tenant.TenantStatus tenantStatus = Tenant.TenantStatus.valueOf(status);
        List<Tenant> tenants = tenantRepository.findByStatus(tenantStatus);
        return tenants.stream().map(this::toTenantResponse).collect(Collectors.toList());
    }

    /**
     * Search tenants
     */
    public List<TenantResponse> searchTenants(String query, int limit) {
        List<Tenant> tenants = tenantRepository.searchTenants(query, limit);
        return tenants.stream().map(this::toTenantResponse).collect(Collectors.toList());
    }

    /**
     * Get tenant analytics
     */
    public Map<String, Object> getTenantAnalytics(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("tenantInfo", toTenantResponse(tenant));

        // Get user counts
        List<User> tenantUsers = userRepository.findByTenantId(tenant.getIdentifier());
        long adminUsers = tenantUsers.stream().filter(u -> u.getPrimaryRole() == User.UserRole.ADMIN).count();
        long teacherUsers = tenantUsers.stream().filter(u -> u.getPrimaryRole() == User.UserRole.TEACHER).count();
        long studentUsers = tenantUsers.stream().filter(u -> u.getPrimaryRole() == User.UserRole.STUDENT).count();
        long parentUsers = tenantUsers.stream().filter(u -> u.getPrimaryRole() == User.UserRole.PARENT).count();

        analytics.put("userCounts", Map.of(
            "total", tenantUsers.size(),
            "admins", adminUsers,
            "teachers", teacherUsers,
            "students", studentUsers,
            "parents", parentUsers
        ));

        // Usage percentages
        if (tenant.getMaxStudents() > 0) {
            analytics.put("studentUsagePercentage", (tenant.getCurrentStudents() * 100.0) / tenant.getMaxStudents());
        }
        if (tenant.getMaxTeachers() > 0) {
            analytics.put("teacherUsagePercentage", (tenant.getCurrentTeachers() * 100.0) / tenant.getMaxTeachers());
        }
        if (tenant.getMaxStorageGb() > 0) {
            analytics.put("storageUsagePercentage", (tenant.getCurrentStorageMb() / 1024.0) * 100.0 / tenant.getMaxStorageGb());
        }

        return analytics;
    }

    /**
     * Bulk activate tenants
     */
    public Map<String, String> bulkActivateTenants(List<UUID> tenantIds, String reason) {
        Map<String, String> results = new HashMap<>();

        for (UUID tenantId : tenantIds) {
            try {
                activateTenant(tenantId, reason);
                results.put(tenantId.toString(), "SUCCESS");
            } catch (Exception e) {
                results.put(tenantId.toString(), "FAILED: " + e.getMessage());
                log.error("Failed to activate tenant: {}", tenantId, e);
            }
        }

        return results;
    }

    /**
     * Bulk suspend tenants
     */
    public Map<String, String> bulkSuspendTenants(List<UUID> tenantIds, String reason) {
        Map<String, String> results = new HashMap<>();

        for (UUID tenantId : tenantIds) {
            try {
                suspendTenant(tenantId, reason);
                results.put(tenantId.toString(), "SUCCESS");
            } catch (Exception e) {
                results.put(tenantId.toString(), "FAILED: " + e.getMessage());
                log.error("Failed to suspend tenant: {}", tenantId, e);
            }
        }

        return results;
    }

    /**
     * Get tenant configuration
     */
    public Map<String, Object> getTenantConfiguration(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Map<String, Object> config = new HashMap<>();
        
        if (tenant.getConfiguration() != null) {
            // Parse JSON configuration
            try {
                // This would need JSON parsing - simplified for now
                config.put("config", tenant.getConfiguration());
            } catch (Exception e) {
                log.error("Error parsing tenant configuration", e);
                config.put("config", tenant.getConfiguration());
            }
        }

        return config;
    }

    /**
     * Update tenant configuration
     */
    public void updateTenantConfiguration(UUID tenantId, Map<String, Object> configuration) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        // Convert configuration to JSON string
        tenant.setConfiguration(configuration.toString());
        tenantRepository.save(tenant);

        log.info("SuperAdmin updated configuration for tenant: {}", tenantId);
    }

    // Private helper methods

    private void validateTenantRequest(TenantRegistrationRequest request) {
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new BusinessException("Subdomain already exists: " + request.getSubdomain());
        }

        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }
    }

    private void setDefaultLimits(Tenant tenant, String subscriptionPlan) {
        switch (subscriptionPlan) {
            case "TRIAL":
                tenant.setMaxStudents(50);
                tenant.setMaxTeachers(10);
                tenant.setMaxStorageGb(1);
                break;
            case "BASIC":
                tenant.setMaxStudents(200);
                tenant.setMaxTeachers(25);
                tenant.setMaxStorageGb(5);
                break;
            case "STANDARD":
                tenant.setMaxStudents(500);
                tenant.setMaxTeachers(50);
                tenant.setMaxStorageGb(20);
                break;
            case "PREMIUM":
                tenant.setMaxStudents(1000);
                tenant.setMaxTeachers(100);
                tenant.setMaxStorageGb(50);
                break;
            case "ENTERPRISE":
                tenant.setMaxStudents(-1); // Unlimited
                tenant.setMaxTeachers(-1); // Unlimited
                tenant.setMaxStorageGb(200);
                break;
        }
    }

    private User createTenantAdminUser(Tenant tenant, com.schoolmgmt.dto.common.AdminUserRequest adminUserRequest) {
        if (userRepository.existsByUsernameAndTenantId(adminUserRequest.getUsername(), tenant.getIdentifier())) {
            throw new BusinessException("Username already exists: " + adminUserRequest.getUsername());
        }
        
        String tenantPrefix = tenant.getIdentifier();
        Integer lastSequence = userRepository.findMaxSequenceForTenant(tenantPrefix);
        if (lastSequence == null) {
            lastSequence = 0;
        }
        
        String userId = adminUserRequest.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            userId = String.format("%s%05d", tenantPrefix, lastSequence + 1);
        }
        
        User adminUser = User.builder()
                .userId(userId)
                .username(adminUserRequest.getUsername())
                .email(adminUserRequest.getEmail())
                .password(passwordEncoder.encode(adminUserRequest.getPassword()))
                .firstName(adminUserRequest.getFirstName())
                .lastName(adminUserRequest.getLastName())
                .phone(adminUserRequest.getPhone())
                .role(User.UserRole.ADMIN)
                .emailVerified(true) // SuperAdmin created users are pre-verified
                .isActive(true)
                .accountNonLocked(true)
                .build();
                
        adminUser.setTenantId(tenant.getIdentifier());
        
        User savedUser = userRepository.save(adminUser);
        log.info("SuperAdmin created admin user: {} for tenant: {}", savedUser.getUsername(), tenant.getIdentifier());
        
        return savedUser;
    }

    private Specification<Tenant> buildTenantSpecification(TenantFilterRequest filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (StringUtils.hasText(filter.getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), 
                        "%" + filter.getName().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(filter.getSubdomain())) {
                    predicates.add(cb.like(cb.lower(root.get("subdomain")), 
                        "%" + filter.getSubdomain().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(filter.getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), 
                        "%" + filter.getEmail().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(filter.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), 
                        Tenant.TenantStatus.valueOf(filter.getStatus())));
                }

                if (StringUtils.hasText(filter.getSubscriptionPlan())) {
                    predicates.add(cb.equal(root.get("subscriptionPlan"), 
                        Tenant.SubscriptionPlan.valueOf(filter.getSubscriptionPlan())));
                }

                if (StringUtils.hasText(filter.getCity())) {
                    predicates.add(cb.like(cb.lower(root.get("city")), 
                        "%" + filter.getCity().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(filter.getState())) {
                    predicates.add(cb.like(cb.lower(root.get("state")), 
                        "%" + filter.getState().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(filter.getCountry())) {
                    predicates.add(cb.like(cb.lower(root.get("country")), 
                        "%" + filter.getCountry().toLowerCase() + "%"));
                }

                if (filter.getCreatedAfter() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
                }

                if (filter.getCreatedBefore() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
                }

                if (StringUtils.hasText(filter.getSearch())) {
                    String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchTerm),
                        cb.like(cb.lower(root.get("subdomain")), searchTerm),
                        cb.like(cb.lower(root.get("email")), searchTerm)
                    ));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private void updateTenantFields(Tenant tenant, UpdateTenantRequest request) {
        if (StringUtils.hasText(request.getName())) {
            tenant.setName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            tenant.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            tenant.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAddress())) {
            tenant.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getCity())) {
            tenant.setCity(request.getCity());
        }
        if (StringUtils.hasText(request.getState())) {
            tenant.setState(request.getState());
        }
        if (StringUtils.hasText(request.getCountry())) {
            tenant.setCountry(request.getCountry());
        }
        if (StringUtils.hasText(request.getPostalCode())) {
            tenant.setPostalCode(request.getPostalCode());
        }
        if (StringUtils.hasText(request.getWebsite())) {
            tenant.setWebsite(request.getWebsite());
        }
    }

    private String updateConfigWithReason(String existingConfig, String key, String value) {
        // Simple implementation - in production, you'd use proper JSON handling
        if (existingConfig == null) {
            return String.format("{\"%s\": \"%s\"}", key, value);
        }
        // This is a simplified approach - use Jackson for proper JSON handling
        return existingConfig;
    }

    private com.schoolmgmt.dto.common.TenantLimits buildTenantLimits(Tenant tenant) {
        return com.schoolmgmt.dto.common.TenantLimits.builder()
                .maxStudents(tenant.getMaxStudents())
                .maxTeachers(tenant.getMaxTeachers())
                .maxStorageGb(tenant.getMaxStorageGb())
                .currentStudents(tenant.getCurrentStudents())
                .currentTeachers(tenant.getCurrentTeachers())
                .currentStorageMb(tenant.getCurrentStorageMb())
                .build();
    }

    private TenantRegistrationResponse toTenantRegistrationResponse(Tenant tenant) {
        return TenantRegistrationResponse.builder()
                .message("Tenant created successfully")
                .accessUrl("https://" + tenant.getSubdomain() + ".schoolmgmt.com")
                .nextSteps(new String[]{"Login with admin credentials", "Configure school settings", "Add teachers and students"})
                .build();
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId().toString())
                .identifier(tenant.getIdentifier())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .address(tenant.getAddress())
                .city(tenant.getCity())
                .state(tenant.getState())
                .country(tenant.getCountry())
                .postalCode(tenant.getPostalCode())
                .website(tenant.getWebsite())
                .logoUrl(tenant.getLogoUrl())
                .status(tenant.getStatus().name())
                .subscriptionPlan(tenant.getSubscriptionPlan().name())
                .limits(buildTenantLimits(tenant))
                .createdAt(tenant.getCreatedAt())
                .activatedAt(tenant.getActivatedAt())
                .build();
    }
}