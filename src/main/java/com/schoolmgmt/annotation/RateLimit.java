package com.schoolmgmt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for per-endpoint rate limiting.
 * Apply to controller methods to override the default tenant/IP rate limits
 * with a stricter per-endpoint limit.
 *
 * Example usage:
 * <pre>
 *   {@literal @}RateLimit(requests = 5, durationMinutes = 1)
 *   {@literal @}PostMapping("/login")
 *   public ResponseEntity login(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed within the duration window.
     */
    int requests() default 10;

    /**
     * Duration window in minutes.
     */
    int durationMinutes() default 1;
}
