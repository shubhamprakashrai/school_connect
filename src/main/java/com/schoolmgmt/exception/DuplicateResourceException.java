package com.schoolmgmt.exception;

/**
<<<<<<< Updated upstream
 * Exception thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }
=======
 * Exception thrown when a resource already exists
 * (e.g., duplicate class name, duplicate section code, etc.)
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
>>>>>>> Stashed changes
}