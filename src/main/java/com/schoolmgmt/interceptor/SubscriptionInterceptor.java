package com.schoolmgmt.interceptor;

import com.schoolmgmt.model.SchoolSubscription;
import com.schoolmgmt.model.SchoolSubscription.SubscriptionStatus;
import com.schoolmgmt.repository.SchoolSubscriptionRepository;
import com.schoolmgmt.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SchoolSubscriptionRepository subscriptionRepository;

    private static final Set<String> BYPASS_PATHS = Set.of(
            "/auth/", "/webhooks/", "/superadmin/", "/subscription/",
            "/swagger-ui", "/v3/api-docs", "/actuator", "/public/",
            "/config/mobile", "/error"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        for (String path : BYPASS_PATHS) {
            if (uri.contains(path)) return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return true;

        Optional<SchoolSubscription> subOpt = subscriptionRepository.findByTenantId(tenantId);
        if (subOpt.isEmpty()) return true;

        SchoolSubscription subscription = subOpt.get();
        SubscriptionStatus status = subscription.getStatus();

        switch (status) {
            case ACTIVE:
                return true;

            case GRACE:
                response.setHeader("X-Subscription-Warning",
                        "Subscription expired. Grace period active.");
                return true;

            case READ_ONLY:
                String method = request.getMethod();
                if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) ||
                    "OPTIONS".equalsIgnoreCase(method)) {
                    response.setHeader("X-Subscription-Warning",
                            "Account is read-only. Please renew subscription.");
                    return true;
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":\"ERROR\",\"message\":\"Account is read-only. Please renew your subscription to make changes.\"}");
                return false;

            case SUSPENDED:
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":\"ERROR\",\"message\":\"Account suspended. Please contact support or renew subscription.\"}");
                return false;

            case CANCELLED:
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":\"ERROR\",\"message\":\"Subscription cancelled.\"}");
                return false;

            default:
                return true;
        }
    }
}
