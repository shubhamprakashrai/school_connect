package com.schoolmgmt.config;

import com.schoolmgmt.model.Tenant.SubscriptionPlan;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Manages per-tenant and per-IP rate limit buckets stored in-memory.
 * Redis-based storage can be added later for distributed deployments.
 */
@Component
@Slf4j
public class RateLimitConfig {

    /**
     * Rate limits per minute for each subscription plan.
     */
    private static final Map<SubscriptionPlan, Integer> PLAN_LIMITS = Map.of(
            SubscriptionPlan.TRIAL, 100,
            SubscriptionPlan.BASIC, 100,
            SubscriptionPlan.STANDARD, 600,
            SubscriptionPlan.PREMIUM, 1200,
            SubscriptionPlan.ENTERPRISE, Integer.MAX_VALUE
    );

    /**
     * Default rate limit for unauthenticated (per-IP) requests: 60 requests/minute.
     */
    private static final int DEFAULT_IP_LIMIT = 60;

    private final ConcurrentHashMap<String, Bucket> tenantBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    /**
     * Get or create a rate limit bucket for a tenant based on its subscription plan.
     *
     * @param tenantId         the tenant identifier
     * @param subscriptionPlan the tenant's subscription plan
     * @return the rate limit bucket
     */
    public Bucket resolveTenantBucket(String tenantId, SubscriptionPlan subscriptionPlan) {
        return tenantBuckets.computeIfAbsent(tenantId, key -> createBucket(subscriptionPlan));
    }

    /**
     * Get or create a rate limit bucket for an IP address (unauthenticated requests).
     *
     * @param ipAddress the client IP address
     * @return the rate limit bucket
     */
    public Bucket resolveIpBucket(String ipAddress) {
        return ipBuckets.computeIfAbsent(ipAddress, key -> createIpBucket());
    }

    /**
     * Get the rate limit for a subscription plan.
     *
     * @param plan the subscription plan
     * @return requests per minute allowed
     */
    public int getLimitForPlan(SubscriptionPlan plan) {
        return PLAN_LIMITS.getOrDefault(plan, PLAN_LIMITS.get(SubscriptionPlan.BASIC));
    }

    /**
     * Get the default IP rate limit.
     *
     * @return requests per minute for unauthenticated requests
     */
    public int getIpLimit() {
        return DEFAULT_IP_LIMIT;
    }

    /**
     * Check if a subscription plan has unlimited requests.
     *
     * @param plan the subscription plan
     * @return true if the plan has no rate limit
     */
    public boolean isUnlimited(SubscriptionPlan plan) {
        return plan == SubscriptionPlan.ENTERPRISE;
    }

    /**
     * Evict a tenant's bucket (e.g., when plan changes).
     *
     * @param tenantId the tenant identifier
     */
    public void evictTenantBucket(String tenantId) {
        tenantBuckets.remove(tenantId);
        log.info("Evicted rate limit bucket for tenant: {}", tenantId);
    }

    private Bucket createBucket(SubscriptionPlan plan) {
        int limit = getLimitForPlan(plan);
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private Bucket createIpBucket() {
        Bandwidth bandwidth = Bandwidth.classic(DEFAULT_IP_LIMIT, Refill.greedy(DEFAULT_IP_LIMIT, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
