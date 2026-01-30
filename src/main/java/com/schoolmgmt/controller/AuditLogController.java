package com.schoolmgmt.controller;

import com.schoolmgmt.model.AuditLog;
import com.schoolmgmt.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit Logs", description = "System audit log APIs")
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AuditLog> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all audit logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAll(
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/entity/{entityType}")
    @Operation(summary = "Get audit logs by entity type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getByEntityType(@PathVariable String entityType,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getByEntityType(entityType, pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getByUser(@PathVariable String userId,
            @PageableDefault(size = 20, sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getByUserId(userId, pageable));
    }
}
