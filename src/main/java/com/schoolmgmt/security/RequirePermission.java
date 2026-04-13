package com.schoolmgmt.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller handler method as requiring one of the listed
 * permissions. The interceptor {@link PermissionHandlerInterceptor}
 * reads the annotation and delegates to {@code PermissionService.require}
 * before the handler runs.
 *
 * Usage:
 * <pre>{@code
 *   @PostMapping("/fees/submissions/{id}/approve")
 *   @RequirePermission(Permission.FEES_APPROVE)
 *   public ResponseEntity<Void> approve(@PathVariable UUID id) { ... }
 * }</pre>
 *
 * If multiple keys are supplied, the user must hold ANY one of them
 * (logical OR).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String[] value();
}
