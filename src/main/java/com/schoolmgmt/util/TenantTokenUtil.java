package com.schoolmgmt.util;

import com.schoolmgmt.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class to extract tenant and student login information from JWT tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantTokenUtil {

    private final JwtService jwtService;

    /**
     * Extract student login required flag from current request's JWT token
     * @return Boolean value of studentLoginRequired, or null if not found
     */
    public Boolean extractStudentLoginRequiredFromCurrentToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String token = extractTokenFromRequest(request);
            if (token != null) {
                return jwtService.extractStudentLoginRequired(token);
            }
        } catch (Exception e) {
            // Log error if needed, but don't throw to avoid breaking the flow
        }
        return null;
    }

    /**
     * Extract tenant identifier from current request's JWT token
     * @return String tenant identifier, or null if not found
     */
    public String extractTenantIdFromCurrentToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String token = extractTokenFromRequest(request);
            if (token != null) {
                return jwtService.extractTenantId(token);
            }
        } catch (Exception e) {
            // Log error if needed, but don't throw to avoid breaking the flow
        }
        return null;
    }

    /**
     * Extract user role from current request's JWT token
     * @return String user role, or null if not found
     */
    public String extractRoleFromCurrentToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String token = extractTokenFromRequest(request);
            if (token != null) {
                return jwtService.extractRole(token);
            }
        } catch (Exception e) {
            // Log error if needed, but don't throw to avoid breaking the flow
        }
        return null;
    }

    /**
     * Extract username from current request's JWT token
     * @return String username, or null if not found
     */
    public String extractUsernameFromCurrentToken() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String token = extractTokenFromRequest(request);
            if (token != null) {
                // Use the existing jwtService.extractUsername method 
                // This returns the "sub" claim, which should be the username/email
                String username = jwtService.extractUsername(token);
                log.debug("Extracted username from token: {}", username);
                return username;
            }
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
        }
        return null;
    }


    /**
     * Check if student login is required for the current tenant
     * @return true if student login is required, false otherwise
     */
    public boolean isStudentLoginRequired() {
        Boolean studentLoginRequired = extractStudentLoginRequiredFromCurrentToken();
        return studentLoginRequired != null && studentLoginRequired;
    }

    /**
     * Get current HTTP request
     * @return HttpServletRequest
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Extract JWT token from Authorization header
     * @param request HTTP request
     * @return JWT token without "Bearer " prefix, or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract student login required flag from a specific JWT token string
     * @param token JWT token string
     * @return Boolean value of studentLoginRequired, or null if not found
     */
    public Boolean extractStudentLoginRequiredFromToken(String token) {
        try {
            return jwtService.extractStudentLoginRequired(token);
        } catch (Exception e) {
            // Log error if needed, but don't throw to avoid breaking the flow
            return null;
        }
    }

    /**
     * Extract tenant identifier from a specific JWT token string
     * @param token JWT token string
     * @return String tenant identifier, or null if not found
     */
    public String extractTenantIdFromToken(String token) {
        try {
            return jwtService.extractTenantId(token);
        } catch (Exception e) {
            // Log error if needed, but don't throw to avoid breaking the flow
            return null;
        }
    }
}
