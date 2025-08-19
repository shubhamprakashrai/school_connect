package com.schoolmgmt.model;

/**
 * Interface for entities that are tenant-aware.
 * All entities implementing this interface will have tenant-based data isolation.
 */
public interface TenantAware {
    
    /**
     * Get the tenant ID associated with this entity
     * @return The tenant ID
     */
    String getTenantId();
    
    /**
     * Set the tenant ID for this entity
     * @param tenantId The tenant ID
     */
    void setTenantId(String tenantId);
}
