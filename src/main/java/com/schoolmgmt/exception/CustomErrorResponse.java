package com.schoolmgmt.exception;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    private String details;
}