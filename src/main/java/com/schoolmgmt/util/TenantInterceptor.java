package com.schoolmgmt.util;

import com.schoolmgmt.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to resolve and set tenant context for each request.
 * Works in coordination with JwtAuthenticationFilter for authenticated requests.
 * Handles tenant resolution for non-JWT requests (like login, registration).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;
    private final TenantResolver tenantResolver;

    @Value("${multitenancy.strategy:HEADER}")
    private String tenantStrategy;

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Skip tenant resolution for public endpoints
        if (isPublicEndpoint(requestURI)) {
            log.debug("Skipping tenant resolution for public endpoint: {}", requestURI);
            return true;
        }

        // Check if tenant context is already set by JwtAuthenticationFilter
        String existingTenant = TenantContext.getCurrentTenant();
        if (existingTenant != null) {
            log.debug("Tenant context already set by JWT filter: {}", existingTenant);
            return true;
        }

        try {
            // Resolve tenant for non-JWT requests (login, registration, etc.)
            String tenantId = resolveTenant(request);

            if (tenantId == null) {
                log.warn("No tenant found for request: {}", requestURI);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identification required");
                return false;
            }

            // Validate tenant exists and is active
            if (!tenantResolver.validateTenant(tenantId)) {
                log.warn("Invalid or inactive tenant: {}", tenantId);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or inactive tenant");
                return false;
            }

            // Set tenant context
            TenantContext.setCurrentTenant(tenantId);
            log.debug("Tenant context set by interceptor for request: {} -> {}", requestURI, tenantId);

            return true;

        } catch (Exception e) {
            log.error("Error resolving tenant for request: {}", requestURI, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing tenant information");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Add tenant info to response headers for debugging
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            response.setHeader("X-Current-Tenant", currentTenant);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clear tenant context after request completion
        // But only if it was set by this interceptor, not by JWT filter
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            TenantContext.clear();
            log.debug("Tenant context cleared for request: {}", request.getRequestURI());
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        String tenantId = null;

        switch (tenantStrategy.toUpperCase()) {
            case "HEADER":
                tenantId = resolveFromHeader(request);
                break;
            case "SUBDOMAIN":
                tenantId = resolveFromSubdomain(request);
                break;
            case "JWT":
                // JWT resolution is handled in JwtAuthenticationFilter
                tenantId = resolveFromHeader(request); // Fallback to header
                break;
            case "HYBRID":
                // Try header first for non-JWT requests
                tenantId = resolveFromHeader(request);
                if (tenantId == null) {
                    tenantId = resolveFromSubdomain(request);
                }
                break;
            default:
                log.warn("Unknown tenant strategy: {}", tenantStrategy);
                tenantId = resolveFromHeader(request);
        }

        return tenantId;
    }

    private String resolveFromHeader(HttpServletRequest request) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null) {
            log.debug("Resolved tenant from header: {}", tenantId);
        }
        return tenantId;
    }

    private String resolveFromSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        if (host != null && !host.startsWith("www.")) {
            String[] parts = host.split("\\.");
            if (parts.length > 2) {
                String subdomain = parts[0];
                log.debug("Resolved tenant from subdomain: {}", subdomain);
                return tenantResolver.getTenantIdBySubdomain(subdomain);
            }
        }
        return null;
    }

    private boolean isPublicEndpoint(String uri) {
        return uri.contains("/public/") ||
                uri.contains("/swagger-ui") ||
                uri.contains("/v3/api-docs") ||
                uri.contains("/actuator/health") ||
                uri.contains("/auth/login") ||
                uri.contains("/auth/register") ||
                uri.contains("/tenants/register") ||
                uri.equals("/error");
    }
}