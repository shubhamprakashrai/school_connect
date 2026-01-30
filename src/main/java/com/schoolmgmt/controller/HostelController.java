package com.schoolmgmt.controller;

import com.schoolmgmt.model.Hostel;
import com.schoolmgmt.model.HostelAllocation;
import com.schoolmgmt.model.HostelRoom;
import com.schoolmgmt.service.HostelService;
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
@RequestMapping("/api/hostel")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hostel Management", description = "Hostel management APIs")
public class HostelController {

    private final HostelService service;

    @PostMapping
    @Operation(summary = "Create hostel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Hostel> createHostel(@RequestBody Hostel hostel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createHostel(hostel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hostel by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Hostel> getHostelById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getHostelById(id));
    }

    @GetMapping
    @Operation(summary = "Get all hostels")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<Hostel>> getAllHostels(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.getAllHostels(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update hostel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Hostel> updateHostel(@PathVariable UUID id, @RequestBody Hostel hostel) {
        return ResponseEntity.ok(service.updateHostel(id, hostel));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hostel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteHostel(@PathVariable UUID id) {
        service.deleteHostel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms")
    @Operation(summary = "Create hostel room")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HostelRoom> createRoom(@RequestBody HostelRoom room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRoom(room));
    }

    @GetMapping("/{hostelId}/rooms")
    @Operation(summary = "Get rooms by hostel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<HostelRoom>> getRoomsByHostel(@PathVariable String hostelId,
            @PageableDefault(size = 20, sort = "roomNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.getRoomsByHostel(hostelId, pageable));
    }

    @PostMapping("/allocations")
    @Operation(summary = "Create hostel allocation")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HostelAllocation> createAllocation(@RequestBody HostelAllocation allocation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAllocation(allocation));
    }

    @GetMapping("/allocations")
    @Operation(summary = "Get all allocations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<Page<HostelAllocation>> getAllAllocations(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAllAllocations(pageable));
    }
}
