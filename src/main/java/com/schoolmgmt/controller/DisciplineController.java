package com.schoolmgmt.controller;

import com.schoolmgmt.model.DisciplineRecord;
import com.schoolmgmt.service.DisciplineRecordService;
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
@RequestMapping("/api/discipline")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Discipline", description = "Discipline and conduct tracking APIs")
public class DisciplineController {

    private final DisciplineRecordService service;

    @PostMapping
    @Operation(summary = "Create discipline record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<DisciplineRecord> create(@RequestBody DisciplineRecord record) {
        log.info("Creating discipline record for student: {}", record.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(record));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discipline record by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<DisciplineRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all discipline records")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<DisciplineRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get discipline records by student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<List<DisciplineRecord>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update discipline record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DisciplineRecord> update(@PathVariable UUID id, @RequestBody DisciplineRecord record) {
        return ResponseEntity.ok(service.update(id, record));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete discipline record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
