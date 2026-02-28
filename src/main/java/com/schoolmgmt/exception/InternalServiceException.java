package com.schoolmgmt.exception;

/**
 * Exception thrown for internal server errors in business logic.
 */
public class InternalServiceException extends RuntimeException {
    public InternalServiceException(String message) {
        super(message);
    }
    public InternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

