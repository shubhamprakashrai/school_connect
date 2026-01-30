package com.schoolmgmt.controller;

import com.schoolmgmt.model.Album;
import com.schoolmgmt.model.Photo;
import com.schoolmgmt.service.GalleryService;
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
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gallery Management", description = "APIs for managing photo albums and gallery")
public class GalleryController {

    private final GalleryService galleryService;

    // ===== Album Endpoints =====

    @PostMapping("/albums")
    @Operation(summary = "Create a photo album")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Album> createAlbum(@Valid @RequestBody Album album) {
        log.info("Creating album: {}", album.getTitle());
        Album created = galleryService.createAlbum(album);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/albums")
    @Operation(summary = "Get paginated albums")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Page<Album>> getAllAlbums(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(galleryService.getAllAlbums(pageable));
    }

    @GetMapping("/albums/{id}")
    @Operation(summary = "Get album by ID")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Album> getAlbumById(@PathVariable UUID id) {
        return ResponseEntity.ok(galleryService.getAlbumById(id));
    }

    @PutMapping("/albums/{id}")
    @Operation(summary = "Update album")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Album> updateAlbum(@PathVariable UUID id, @Valid @RequestBody Album album) {
        return ResponseEntity.ok(galleryService.updateAlbum(id, album));
    }

    @DeleteMapping("/albums/{id}")
    @Operation(summary = "Delete a photo album")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID id) {
        galleryService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Photo Endpoints =====

    @PostMapping("/albums/{id}/photos")
    @Operation(summary = "Add photo to album")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Photo> addPhoto(@PathVariable UUID id, @Valid @RequestBody Photo photo) {
        log.info("Adding photo to album: {}", id);
        Photo added = galleryService.addPhoto(id, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @DeleteMapping("/photos/{id}")
    @Operation(summary = "Remove photo from album")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> removePhoto(@PathVariable UUID id) {
        galleryService.removePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/albums/{id}/photos")
    @Operation(summary = "Get all photos in an album")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<Photo>> getAlbumPhotos(@PathVariable UUID id) {
        return ResponseEntity.ok(galleryService.getAlbumPhotos(id));
    }
}
