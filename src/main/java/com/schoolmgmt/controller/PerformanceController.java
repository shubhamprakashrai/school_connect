package com.schoolmgmt.controller;

import com.schoolmgmt.model.PerformanceReview;
import com.schoolmgmt.service.PerformanceReviewService;
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
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Performance Reviews", description = "Staff performance evaluation APIs")
public class PerformanceController {

    private final PerformanceReviewService service;

    @PostMapping
    @Operation(summary = "Create performance review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PerformanceReview> create(@RequestBody PerformanceReview review) {
        log.info("Creating performance review for staff: {}", review.getStaffId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(review));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get performance review by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PerformanceReview> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all performance reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<PerformanceReview>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get reviews by staff member")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PerformanceReview>> getByStaff(@PathVariable String staffId) {
        return ResponseEntity.ok(service.getByStaffId(staffId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update performance review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PerformanceReview> update(@PathVariable UUID id, @RequestBody PerformanceReview review) {
        return ResponseEntity.ok(service.update(id, review));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete performance review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
