package com.schoolmgmt.controller;

import com.schoolmgmt.model.Complaint;
import com.schoolmgmt.service.ComplaintService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Complaints", description = "Complaint and grievance system APIs")
public class ComplaintController {

    private final ComplaintService service;

    @PostMapping
    @Operation(summary = "Create complaint")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<Complaint> create(@RequestBody Complaint complaint) {
        log.info("Creating complaint: {}", complaint.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(complaint));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<Complaint> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all complaints")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<Complaint>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get complaints by status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Complaint>> getByStatus(@PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getByStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update complaint")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Complaint> update(@PathVariable UUID id, @RequestBody Complaint complaint) {
        return ResponseEntity.ok(service.update(id, complaint));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete complaint")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
