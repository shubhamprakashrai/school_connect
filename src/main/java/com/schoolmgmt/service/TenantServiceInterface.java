package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.TenantRegistrationRequest;
import com.schoolmgmt.dto.request.UpdateTenantRequest;
import com.schoolmgmt.dto.response.TenantRegistrationResponse;
import com.schoolmgmt.dto.response.TenantResponse;
import com.schoolmgmt.dto.response.TenantStatistics;

import java.util.UUID;

public interface TenantServiceInterface {



    /**
     * Register a new school/tenant
     */
    TenantRegistrationResponse registerTenant(TenantRegistrationRequest request);

    /**
     * Get current tenant information
     */
    TenantResponse getCurrentTenant();

    /**
     * Update tenant information
     */
    TenantResponse updateTenant(UUID tenantId, UpdateTenantRequest request);

    /**
     * Activate a tenant
     */
    void activateTenant(UUID tenantId);

    /**
     * Suspend a tenant
     */
    void suspendTenant(UUID tenantId, String reason);

    /**
     * Get tenant statistics
     */
    TenantStatistics getTenantStatistics();

    /**
     * Check if tenant can add more students
     */
    boolean canAddStudent();

    /**
     * Check if tenant can add more teachers
     */
    boolean canAddTeacher();

    /**
     * Update tenant resource usage
     */
    void updateStudentCount(int delta);

    void updateTeacherCount(int delta);

    void updateStorageUsage(int delta);
}
