package com.schoolmgmt.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to ensure tenant context is always cleaned up after request processing.
 * This prevents memory leaks and context pollution between requests.
 * Runs with highest priority to ensure cleanup happens regardless of other filter failures.
 */
@Component
@Order(Integer.MAX_VALUE) // Run last
@Slf4j
public class TenantCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up tenant context, regardless of what happens
            String tenantBeforeCleanup = TenantContext.getCurrentTenant();
            if (tenantBeforeCleanup != null) {
                TenantContext.clear();
                log.debug("Cleaned up tenant context: {} for request: {}",
                        tenantBeforeCleanup, request.getRequestURI());
            }
        }
    }
}