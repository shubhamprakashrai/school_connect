package com.schoolmgmt.exception;

/**
 * Exception thrown when email sending fails critically.
 */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message) {
        super(message);
    }
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}

