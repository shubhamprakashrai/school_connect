package com.schoolmgmt.exception;

/**
 * Exception thrown when a class teacher assignment is not found or invalid.
 */
public class ClassTeacherException extends RuntimeException {

    private final String errorCode;

    public ClassTeacherException(String message) {
        super(message);
        this.errorCode = "CLASS_TEACHER_ERROR";
    }

    public ClassTeacherException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ClassTeacherException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CLASS_TEACHER_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
