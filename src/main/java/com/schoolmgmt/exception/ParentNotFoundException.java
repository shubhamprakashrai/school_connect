package com.schoolmgmt.exception;

public class ParentNotFoundException extends RuntimeException {
    public ParentNotFoundException(String message) {
        super(message);
    }
}
