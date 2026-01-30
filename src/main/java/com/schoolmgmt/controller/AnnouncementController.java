package com.schoolmgmt.controller;

import com.schoolmgmt.model.Announcement;
import com.schoolmgmt.service.AnnouncementService;
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
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Announcement Management", description = "APIs for managing school announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @Operation(summary = "Create announcement")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Announcement> createAnnouncement(@Valid @RequestBody Announcement announcement) {
        log.info("Creating announcement: {}", announcement.getTitle());
        Announcement created = announcementService.create(announcement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get paginated announcements")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Page<Announcement>> getAnnouncements(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(announcementService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable UUID id) {
        return ResponseEntity.ok(announcementService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update announcement")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable UUID id,
            @Valid @RequestBody Announcement announcement) {
        return ResponseEntity.ok(announcementService.update(id, announcement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete announcement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable UUID id) {
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    @Operation(summary = "Get announcements by role")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<Announcement>> getAnnouncementsByRole(@RequestParam String role) {
        return ResponseEntity.ok(announcementService.getByRole(role));
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish announcement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Announcement> publishAnnouncement(@PathVariable UUID id) {
        return ResponseEntity.ok(announcementService.publish(id));
    }

    @PutMapping("/{id}/toggle-pin")
    @Operation(summary = "Toggle pin on announcement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Announcement> unpublishAnnouncement(@PathVariable UUID id) {
        return ResponseEntity.ok(announcementService.unpublish(id));
    }
}
