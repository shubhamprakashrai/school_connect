package com.schoolmgmt.security;

import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RequirePermission} annotations on controller methods.
 * Runs after JWT authentication has populated the security context —
 * so the principal (a {@link User}) is available.
 *
 * Any handler method annotated with {@code @RequirePermission(KEY)} will
 * throw {@link AccessDeniedException} if the current user's effective
 * permission set (see {@link PermissionService#effectivePermissions})
 * does not contain at least one of the listed keys.
 */
@Component
@RequiredArgsConstructor
public class PermissionHandlerInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return true;
        RequirePermission ann = hm.getMethodAnnotation(RequirePermission.class);
        if (ann == null) return true;

        User user = currentUser();
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        var keys = ann.value();
        for (String key : keys) {
            if (permissionService.hasPermission(user, key)) return true;
        }
        throw new AccessDeniedException(
            "Missing required permission: " + String.join(" or ", keys));
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) return u;
        if (principal instanceof String email) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
