package com.schoolmgmt.infrastructure.exception;

/**
 * Exception thrown when a tenant is not found or invalid.
 */
public class TenantNotFoundException extends RuntimeException {
    
    private final String tenantId;
    
    public TenantNotFoundException(String tenantId) {
        super(String.format("Tenant not found: %s", tenantId));
        this.tenantId = tenantId;
    }
    
    public TenantNotFoundException(String message, String tenantId) {
        super(message);
        this.tenantId = tenantId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
}
