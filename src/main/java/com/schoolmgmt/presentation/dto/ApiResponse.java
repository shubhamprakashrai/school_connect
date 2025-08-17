package com.schoolmgmt.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generic API response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response")
public class ApiResponse {

    @Schema(description = "Response status", example = "SUCCESS")
    private ResponseStatus status;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private Object data;

    @Schema(description = "Error details")
    private ErrorDetails error;

    @Schema(description = "Response timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Convenience factory methods
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .message(message)
                .build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .status(ResponseStatus.ERROR)
                .message(message)
                .build();
    }

    public static ApiResponse error(String message, ErrorDetails error) {
        return ApiResponse.builder()
                .status(ResponseStatus.ERROR)
                .message(message)
                .error(error)
                .build();
    }

    public static ApiResponse validationError(String message, Map<String, List<String>> fieldErrors) {
        ErrorDetails error = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .details("Validation failed")
                .fieldErrors(fieldErrors)
                .build();
        
        return ApiResponse.builder()
                .status(ResponseStatus.ERROR)
                .message(message)
                .error(error)
                .build();
    }

    public enum ResponseStatus {
        SUCCESS,
        ERROR,
        WARNING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        
        @Schema(description = "Error code", example = "AUTH_001")
        private String code;
        
        @Schema(description = "Error details", example = "Invalid credentials provided")
        private String details;
        
        @Schema(description = "Field validation errors")
        private Map<String, List<String>> fieldErrors;
        
        @Schema(description = "Stack trace for debugging (only in dev mode)")
        private String stackTrace;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableResponse<T> {
        
        @Schema(description = "Page content")
        private List<T> content;
        
        @Schema(description = "Current page number", example = "0")
        private int pageNumber;
        
        @Schema(description = "Page size", example = "20")
        private int pageSize;
        
        @Schema(description = "Total elements", example = "100")
        private long totalElements;
        
        @Schema(description = "Total pages", example = "5")
        private int totalPages;
        
        @Schema(description = "Is first page", example = "true")
        private boolean first;
        
        @Schema(description = "Is last page", example = "false")
        private boolean last;
        
        @Schema(description = "Has next page", example = "true")
        private boolean hasNext;
        
        @Schema(description = "Has previous page", example = "false")
        private boolean hasPrevious;
    }
}
