package com.schoolmgmt.controller;

import com.schoolmgmt.model.HealthRecord;
import com.schoolmgmt.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Health Records", description = "Student health and medical record APIs")
public class HealthRecordController {

    private final HealthRecordService service;

    @PostMapping
    @Operation(summary = "Create health record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<HealthRecord> create(@RequestBody HealthRecord record) {
        log.info("Creating health record for student: {}", record.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(record));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get health record by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<HealthRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all health records")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<HealthRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get health records by student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<List<HealthRecord>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update health record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HealthRecord> update(@PathVariable UUID id, @RequestBody HealthRecord record) {
        return ResponseEntity.ok(service.update(id, record));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete health record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
