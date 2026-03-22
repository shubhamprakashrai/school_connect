package com.schoolmgmt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.config.RateLimitConfig;
import com.schoolmgmt.model.Tenant;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.util.TenantContext;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter that enforces request limits per tenant and per IP.
 *
 * <p>For authenticated requests with a tenant context, rate limits are applied
 * based on the tenant's subscription plan. For unauthenticated requests,
 * rate limits are applied per client IP address.</p>
 *
 * <p>Health check and actuator endpoints are excluded from rate limiting.</p>
 *
 * <p>Response headers included on every request:
 * <ul>
 *   <li>X-RateLimit-Limit - maximum requests allowed per window</li>
 *   <li>X-RateLimit-Remaining - remaining requests in current window</li>
 *   <li>X-RateLimit-Reset - epoch seconds when the window resets</li>
 * </ul>
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip rate limiting for health check and actuator endpoints
        return path.startsWith("/api/actuator")
                || path.equals("/api/actuator/health")
                || path.startsWith("/actuator")
                || path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantId = resolveTenantId(request);

        if (tenantId != null) {
            handleTenantRateLimit(tenantId, request, response, filterChain);
        } else {
            handleIpRateLimit(request, response, filterChain);
        }
    }

    /**
     * Apply rate limiting based on the tenant's subscription plan.
     */
    private void handleTenantRateLimit(
            String tenantId,
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Optional<Tenant> tenantOpt = tenantRepository.findByIdentifier(tenantId);

        if (tenantOpt.isEmpty()) {
            // Tenant not found - fall back to IP-based rate limiting
            handleIpRateLimit(request, response, filterChain);
            return;
        }

        Tenant tenant = tenantOpt.get();
        Tenant.SubscriptionPlan plan = tenant.getSubscriptionPlan();

        // Enterprise plan has unlimited requests
        if (rateLimitConfig.isUnlimited(plan)) {
            addRateLimitHeaders(response, rateLimitConfig.getLimitForPlan(plan), rateLimitConfig.getLimitForPlan(plan), 0);
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = rateLimitConfig.resolveTenantBucket(tenantId, plan);
        int limit = rateLimitConfig.getLimitForPlan(plan);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            addRateLimitHeaders(response, limit, probe.getRemainingTokens(), probe.getNanosToWaitForRefill());
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitExceeded(response, limit, probe.getNanosToWaitForRefill());
        }
    }

    /**
     * Apply rate limiting per client IP for unauthenticated requests.
     */
    private void handleIpRateLimit(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitConfig.resolveIpBucket(clientIp);
        int limit = rateLimitConfig.getIpLimit();

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            addRateLimitHeaders(response, limit, probe.getRemainingTokens(), probe.getNanosToWaitForRefill());
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitExceeded(response, limit, probe.getNanosToWaitForRefill());
        }
    }

    /**
     * Resolve the tenant ID from the tenant context (set by JWT filter),
     * falling back to the X-Tenant-ID header.
     */
    private String resolveTenantId(HttpServletRequest request) {
        // First check the ThreadLocal context (set by JwtAuthenticationFilter)
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return tenantId;
        }
        // Fallback to request header
        return request.getHeader("X-Tenant-ID");
    }

    /**
     * Extract the client IP, respecting reverse proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Add standard rate limit response headers.
     */
    private void addRateLimitHeaders(HttpServletResponse response, int limit, long remaining, long nanosToReset) {
        long resetEpochSeconds = Instant.now().plusNanos(nanosToReset).getEpochSecond();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetEpochSeconds));
    }

    /**
     * Send a 429 Too Many Requests JSON response.
     */
    private void sendRateLimitExceeded(HttpServletResponse response, int limit, long nanosToReset) throws IOException {
        long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(nanosToReset) + 1;
        long resetEpochSeconds = Instant.now().plusNanos(nanosToReset).getEpochSecond();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetEpochSeconds));
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded. Please try again in " + retryAfterSeconds + " seconds.");
        body.put("retryAfterSeconds", retryAfterSeconds);
        body.put("timestamp", Instant.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(body));

        log.warn("Rate limit exceeded. Limit: {}, Retry after: {}s", limit, retryAfterSeconds);
    }
}
