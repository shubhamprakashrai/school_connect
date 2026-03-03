package com.schoolmgmt.exception;

/**

 * Exception thrown when a resource already exists
 * (e.g., duplicate class name, duplicate section code, etc.)
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

}