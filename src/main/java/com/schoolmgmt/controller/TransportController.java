package com.schoolmgmt.controller;

import com.schoolmgmt.model.TransportRoute;
import com.schoolmgmt.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/transport")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transport Management", description = "APIs for managing school transport routes")
public class TransportController {

    private final TransportService transportService;

    @PostMapping("/routes")
    @Operation(summary = "Create a transport route")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TransportRoute> createRoute(@Valid @RequestBody TransportRoute route) {
        log.info("Creating transport route: {}", route.getRouteName());
        TransportRoute created = transportService.createRoute(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/routes")
    @Operation(summary = "Get paginated transport routes")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Page<TransportRoute>> getAllRoutes(
            @PageableDefault(size = 20, sort = "routeNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(transportService.getAllRoutes(pageable));
    }

    @GetMapping("/routes/{id}")
    @Operation(summary = "Get transport route by ID")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<TransportRoute> getRouteById(@PathVariable UUID id) {
        return ResponseEntity.ok(transportService.getRouteById(id));
    }

    @PutMapping("/routes/{id}")
    @Operation(summary = "Update transport route")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TransportRoute> updateRoute(
            @PathVariable UUID id,
            @Valid @RequestBody TransportRoute route) {
        return ResponseEntity.ok(transportService.updateRoute(id, route));
    }

    @DeleteMapping("/routes/{id}")
    @Operation(summary = "Delete a transport route")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRoute(@PathVariable UUID id) {
        transportService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/routes/active")
    @Operation(summary = "Get all active transport routes")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TransportRoute>> getActiveRoutes() {
        return ResponseEntity.ok(transportService.getActiveRoutes());
    }
}
