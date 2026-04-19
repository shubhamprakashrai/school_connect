package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.BulkStudentRequest;
import com.schoolmgmt.dto.response.BulkStudentResponse;
import com.schoolmgmt.service.BulkStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students/bulk")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bulk Student Management", description = "Bulk student management APIs")
public class BulkStudentController {

    private final BulkStudentService bulkStudentService;

    @PostMapping
    @Operation(summary = "Create multiple students", description = "Create multiple students in bulk with validation and error reporting")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BulkStudentResponse> createBulkStudents(@Valid @RequestBody BulkStudentRequest request) {
        log.info("Starting bulk student creation: {} students", String.valueOf(request.getStudents().size()));
        BulkStudentResponse response = bulkStudentService.createBulkStudents(request);
        
        if (response.getFailed() > 0) {
            log.warn("Bulk creation completed with {} successful and {} failed", 
                    response.getSuccessful(), response.getFailed());
        } else {
            log.info("Bulk creation completed successfully: {} students created", response.getSuccessful());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/template")
    @Operation(summary = "Get bulk upload template", description = "Get the required template structure for bulk student upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BulkStudentRequest> getBulkUploadTemplate() {
        log.info("Providing bulk upload template");
        BulkStudentRequest template = bulkStudentService.getBulkUploadTemplate();
        return ResponseEntity.ok(template);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate bulk student data", description = "Validate student data without creating records")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BulkStudentResponse> validateBulkStudents(@Valid @RequestBody BulkStudentRequest request) {
        log.info("Validating bulk student data: {} students", String.valueOf(request.getStudents().size()));
        BulkStudentResponse response = bulkStudentService.validateBulkStudents(request);
        return ResponseEntity.ok(response);
    }
}
