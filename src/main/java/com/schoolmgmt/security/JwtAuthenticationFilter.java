package com.schoolmgmt.security;

import com.schoolmgmt.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication filter to validate tokens and set security context.
 * IMPORTANT: Also handles tenant context setting from JWT token.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // CRITICAL FIX: Extract and set tenant context BEFORE loading user details
                String tenantId = jwtService.extractTenantId(jwt);
                if (tenantId != null) {
                    TenantContext.setCurrentTenant(tenantId);
                    log.debug("Set tenant context from JWT: {}", tenantId);
                } else {
                    // Try to get tenant from header as fallback
                    String headerTenantId = request.getHeader("X-Tenant-ID");
                    if (headerTenantId != null) {
                        TenantContext.setCurrentTenant(headerTenantId);
                        log.debug("Set tenant context from header: {}", headerTenantId);
                    } else {
                        log.warn("No tenant ID found in JWT token or header for user: {}", userEmail);
                        // Allow the filter to continue, but user loading will fail
                    }
                }

                // Now load user details with tenant context available
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for user: {} in tenant: {}", userEmail, tenantId);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Clear tenant context on authentication failure
            TenantContext.clear();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String uri) {
        String normalized = uri.startsWith("/") ? uri : "/" + uri;

        return normalized.equals("/api/tenants/register") ||
                normalized.equals("/error") ||
                normalized.startsWith("/api/auth/") ||
                normalized.startsWith("/api/public/") ||
                normalized.startsWith("/swagger-ui") ||
                normalized.startsWith("/v3/api-docs") ||
                normalized.startsWith("/actuator/health");
    }

}