package com.schoolmgmt.service;

import com.schoolmgmt.dto.common.AdminInfo;
import com.schoolmgmt.dto.common.AdminUserRequest;
import com.schoolmgmt.dto.common.TenantInfo;
import com.schoolmgmt.dto.common.TenantLimits;
import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.dto.response.TenantStatistics;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Tenant;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for tenant management and school onboarding operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.name:School Management System}")
    private String appName;

    /**
     * Register a new school/tenant - Complete Onboarding Process
     */
    public TenantRegistrationResponse registerTenant(TenantRegistrationRequest request) {
        log.info("Starting tenant registration for: {}", request.getName());
        
        // Validate subdomain uniqueness
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new BusinessException("Subdomain already exists: " + request.getSubdomain());
        }
        
        // Validate email uniqueness
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }
        
        // Generate unique tenant identifier
        String tenantIdentifier = generateTenantIdentifier(request.getName());
        
        // Generate schema name
        String schemaName = "school_" + request.getSubdomain().toLowerCase().replace("-", "_");
        
        // Create tenant entity
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
                .status(Tenant.TenantStatus.PENDING)
                .subscriptionPlan(Tenant.SubscriptionPlan.valueOf(request.getSubscriptionPlan()))
                .createdBy("SYSTEM")
                .build();
        
        // Set default limits based on subscription plan
        setDefaultLimits(tenant, request.getSubscriptionPlan());
        
        // Save configuration if provided
        if (request.getConfiguration() != null) {
            tenant.setConfiguration(convertMapToJson(request.getConfiguration()));
        }
        
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created: {} with identifier: {}", savedTenant.getName(), savedTenant.getIdentifier());
        
        // Create admin user for the tenant
        User adminUser = createAdminUser(request.getAdminUser(), savedTenant.getIdentifier());
        log.info("Admin user created: {} for tenant: {}", adminUser.getUsername(), savedTenant.getIdentifier());
        
        // Initialize default data for the tenant
        initializeTenantData(savedTenant);
        
        // Activate tenant for trial or if payment confirmed
        if ("TRIAL".equals(request.getSubscriptionPlan())) {
            activateTenant(savedTenant.getId());
        }
        
        // Send welcome email to admin
        sendWelcomeEmail(savedTenant, adminUser);
        
        // Build response
        String accessUrl = frontendUrl + "/login?tenant=" + savedTenant.getSubdomain();
        
        String[] nextSteps = {
            "1. Check your email for login credentials",
            "2. Login using the provided URL: " + accessUrl,
            "3. Complete school profile setup",
            "4. Add academic year and classes",
            "5. Import or add teachers and students",
            "6. Configure attendance and grading settings"
        };
        
        TenantInfo tenantInfo = TenantInfo.builder()
                .id(savedTenant.getId().toString())
                .identifier(savedTenant.getIdentifier())
                .name(savedTenant.getName())
                .subdomain(savedTenant.getSubdomain())
                .status(savedTenant.getStatus().name())
                .subscriptionPlan(savedTenant.getSubscriptionPlan().name())
                .activatedAt(savedTenant.getActivatedAt())
                .build();
        
        AdminInfo adminInfo = AdminInfo.builder()
                .id(adminUser.getId().toString())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .firstName(adminUser.getFirstName())
                .lastName(adminUser.getLastName())
                .build();
        
        return TenantRegistrationResponse.builder()
                .tenant(tenantInfo)
                .admin(adminInfo)
                .message("School registered successfully! Please check your email for login credentials.")
                .accessUrl(accessUrl)
                .nextSteps(nextSteps)
                .build();
    }

    /**
     * Get current tenant information
     */
    public TenantResponse getCurrentTenant() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        return toTenantResponse(tenant);
    }

    /**
     * Update tenant information
     */
    public TenantResponse updateTenant(UUID tenantId, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        
        // Verify current tenant has access
        String currentTenantId = TenantContext.getCurrentTenant();
        if (!tenant.getIdentifier().equals(currentTenantId)) {
            throw new BusinessException("Access denied to update this tenant");
        }
        
        // Update fields
        if (request.getName() != null) {
            tenant.setName(request.getName());
        }
        if (request.getEmail() != null) {
            tenant.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            tenant.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            tenant.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            tenant.setCity(request.getCity());
        }
        if (request.getState() != null) {
            tenant.setState(request.getState());
        }
        if (request.getCountry() != null) {
            tenant.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            tenant.setPostalCode(request.getPostalCode());
        }
        if (request.getWebsite() != null) {
            tenant.setWebsite(request.getWebsite());
        }
        if (request.getLogoUrl() != null) {
            tenant.setLogoUrl(request.getLogoUrl());
        }
        if (request.getConfiguration() != null) {
            tenant.setConfiguration(convertMapToJson(request.getConfiguration()));
        }
        
        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Tenant updated: {}", updatedTenant.getIdentifier());
        
        return toTenantResponse(updatedTenant);
    }

    /**
     * Activate a tenant
     */
    public void activateTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        
        if (tenant.getStatus() == Tenant.TenantStatus.ACTIVE) {
            throw new BusinessException("Tenant is already active");
        }
        
        tenantRepository.activateTenant(tenantId, "SYSTEM");
        log.info("Tenant activated: {}", tenant.getIdentifier());
    }

    /**
     * Suspend a tenant
     */
    public void suspendTenant(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        
        tenantRepository.suspendTenant(tenantId, "SYSTEM");
        log.info("Tenant suspended: {} - Reason: {}", tenant.getIdentifier(), reason);
        
        // TODO: Send notification to tenant admin
    }

    /**
     * Get tenant statistics
     */
    public TenantStatistics getTenantStatistics() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        // Get user statistics
        Map<String, Long> usersByRole = new HashMap<>();
        for (User.UserRole role : User.UserRole.values()) {
            long count = userRepository.countActiveUsersByRoleAndTenant(role, tenantId);
            if (count > 0) {
                usersByRole.put(role.name(), count);
            }
        }
        
        long totalStudents = usersByRole.getOrDefault("STUDENT", 0L);
        long totalTeachers = usersByRole.getOrDefault("TEACHER", 0L);
        long totalParents = usersByRole.getOrDefault("PARENT", 0L);
        long activeUsers = usersByRole.values().stream().mapToLong(Long::longValue).sum();
        
        // TODO: Get actual statistics from respective services
        Map<String, Long> studentsByClass = new HashMap<>();
        long totalClasses = 0;
        double attendancePercentage = 0.0;
        
        return TenantStatistics.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalParents(totalParents)
                .activeUsers(activeUsers)
                .totalClasses(totalClasses)
                .attendancePercentage(attendancePercentage)
                .storageUsedMb(tenant.getCurrentStorageMb())
                .usersByRole(usersByRole)
                .studentsByClass(studentsByClass)
                .build();
    }

    /**
     * Check if tenant can add more students
     */
    public boolean canAddStudent() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        return tenant.canAddStudent();
    }

    /**
     * Check if tenant can add more teachers
     */
    public boolean canAddTeacher() {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        return tenant.canAddTeacher();
    }

    /**
     * Update tenant resource usage
     */
    public void updateStudentCount(int delta) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        tenantRepository.updateStudentCount(tenant.getId(), delta);
    }

    public void updateTeacherCount(int delta) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        tenantRepository.updateTeacherCount(tenant.getId(), delta);
    }

    public void updateStorageUsage(int deltaMb) {
        String tenantId = TenantContext.requireCurrentTenant();
        
        Tenant tenant = tenantRepository.findByIdentifier(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "identifier", tenantId));
        
        tenantRepository.updateStorageUsage(tenant.getId(), deltaMb);
    }

    /**
     * Generate unique tenant identifier
     */
    private String generateTenantIdentifier(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .substring(0, Math.min(name.length(), 10));
        
        String identifier = base;
        int counter = 1;
        
        while (tenantRepository.existsByIdentifier(identifier)) {
            identifier = base + "_" + String.format("%03d", counter);
            counter++;
        }
        
        return identifier;
    }

    /**
     * Set default limits based on subscription plan
     */
    private void setDefaultLimits(Tenant tenant, String plan) {
        switch (plan) {
            case "TRIAL":
                tenant.setMaxStudents(50);
                tenant.setMaxTeachers(5);
                tenant.setMaxStorageGb(1);
                break;
            case "BASIC":
                tenant.setMaxStudents(200);
                tenant.setMaxTeachers(20);
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
                tenant.setMaxStudents(5000);
                tenant.setMaxTeachers(500);
                tenant.setMaxStorageGb(500);
                break;
            default:
                tenant.setMaxStudents(100);
                tenant.setMaxTeachers(10);
                tenant.setMaxStorageGb(2);
        }
    }

    /**
     * Create admin user for the tenant
     */
    private User createAdminUser(AdminUserRequest request, String tenantIdentifier) {
        // Check if username exists for this tenant
        if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantIdentifier)) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }
        
        User adminUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .primaryRole(User.UserRole.ADMIN)
                .roles(Set.of(User.UserRole.ADMIN))
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true) // Auto-verify admin email
                .enabled(true)
                .build();
        
        adminUser.setTenantId(tenantIdentifier);
        adminUser.setCreatedBy("SYSTEM");
        
        return userRepository.save(adminUser);
    }

    /**
     * Initialize default data for the tenant
     */
    private void initializeTenantData(Tenant tenant) {
        try {
            TenantContext.setCurrentTenant(tenant.getIdentifier());
            
            // TODO: Initialize default data
            // 1. Create default academic year
            // 2. Create default classes (Grade 1-12)
            // 3. Create default subjects
            // 4. Create default fee categories
            // 5. Create default attendance settings
            // 6. Create default grading system
            
            log.info("Default data initialized for tenant: {}", tenant.getIdentifier());
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Send welcome email to admin
     */
    private void sendWelcomeEmail(Tenant tenant, User adminUser) {
        // TODO: Implement proper email template
        String subject = "Welcome to " + appName + " - Your School is Ready!";
        String loginUrl = frontendUrl + "/login?tenant=" + tenant.getSubdomain();
        
        String emailContent = String.format(
            "Dear %s,\n\n" +
            "Welcome to %s! Your school '%s' has been successfully registered.\n\n" +
            "Login Details:\n" +
            "URL: %s\n" +
            "Username: %s\n" +
            "Tenant: %s\n\n" +
            "Please login and complete your school setup.\n\n" +
            "Best regards,\n" +
            "%s Team",
            adminUser.getFullName(),
            appName,
            tenant.getName(),
            loginUrl,
            adminUser.getUsername(),
            tenant.getSubdomain(),
            appName
        );
        
        emailService.sendSimpleEmail(adminUser.getEmail(), subject, emailContent);
    }

    /**
     * Convert map to JSON string
     */
    private String convertMapToJson(Map<String, String> map) {
        // Simple JSON conversion - in production, use ObjectMapper
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        return "{" + map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",")) + "}";
    }

    /**
     * Convert Tenant entity to TenantResponse DTO
     */
    private TenantResponse toTenantResponse(Tenant tenant) {
        TenantLimits limits = TenantLimits.builder()
                .maxStudents(tenant.getMaxStudents())
                .currentStudents(tenant.getCurrentStudents())
                .maxTeachers(tenant.getMaxTeachers())
                .currentTeachers(tenant.getCurrentTeachers())
                .maxStorageGb(tenant.getMaxStorageGb())
                .currentStorageMb(tenant.getCurrentStorageMb())
                .build();
        
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
                .limits(limits)
                .createdAt(tenant.getCreatedAt())
                .activatedAt(tenant.getActivatedAt())
                .build();
    }
}
