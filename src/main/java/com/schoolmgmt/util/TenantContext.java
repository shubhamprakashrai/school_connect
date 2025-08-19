package com.schoolmgmt.util;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal holder for current tenant context.
 * Used to maintain tenant information throughout the request lifecycle.
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<String> currentTenant = new InheritableThreadLocal<>();
    
    private TenantContext() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Set the current tenant identifier
     * @param tenantId The tenant identifier
     */
    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        currentTenant.set(tenantId);
    }
    
    /**
     * Get the current tenant identifier
     * @return The current tenant identifier or null if not set
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }
    
    /**
     * Clear the current tenant context
     * Important: Must be called after request processing to prevent memory leaks
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        currentTenant.remove();
    }
    
    /**
     * Check if tenant context is set
     * @return true if tenant context is set, false otherwise
     */
    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }
    
    /**
     * Validate and get the current tenant
     * @return The current tenant identifier
     * @throws IllegalStateException if no tenant is set
     */
    public static String requireCurrentTenant() {
        String tenant = getCurrentTenant();
        if (tenant == null) {
            throw new IllegalStateException("No tenant set in current context");
        }
        return tenant;
    }
}
