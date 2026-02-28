package com.schoolmgmt.exception;

public class ParentAlreadyExistsException extends RuntimeException {
    public ParentAlreadyExistsException(String message) {
        super(message);
    }
}
