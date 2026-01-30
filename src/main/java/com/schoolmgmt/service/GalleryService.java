package com.schoolmgmt.service;

import com.schoolmgmt.model.Album;
import com.schoolmgmt.model.Photo;
import com.schoolmgmt.repository.AlbumRepository;
import com.schoolmgmt.repository.PhotoRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GalleryService {

    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    // ===== Album Operations =====

    @Transactional
    public Album createAlbum(Album album) {
        String tenantId = TenantContext.getCurrentTenant();
        album.setTenantId(tenantId);
        log.info("Creating album: {} for tenant: {}", album.getTitle(), tenantId);
        return albumRepository.save(album);
    }

    @Transactional
    public Album updateAlbum(UUID albumId, Album updated) {
        Album existing = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found: " + albumId));

        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getCoverImageUrl() != null) existing.setCoverImageUrl(updated.getCoverImageUrl());
        if (updated.getEventDate() != null) existing.setEventDate(updated.getEventDate());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getIsPublished() != null) existing.setIsPublished(updated.getIsPublished());

        return albumRepository.save(existing);
    }

    @Transactional
    public void deleteAlbum(UUID albumId) {
        albumRepository.deleteById(albumId);
    }

    @Transactional(readOnly = true)
    public Page<Album> getAllAlbums(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return albumRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Album getAlbumById(UUID albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found: " + albumId));
    }

    // ===== Photo Operations =====

    @Transactional
    public Photo addPhoto(UUID albumId, Photo photo) {
        String tenantId = TenantContext.getCurrentTenant();
        photo.setTenantId(tenantId);
        photo.setAlbumId(albumId);
        photo.setUploadedAt(LocalDateTime.now());

        log.info("Adding photo to album: {}", albumId);
        Photo saved = photoRepository.save(photo);

        // Update album photo count
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found: " + albumId));
        Long count = photoRepository.countByAlbumIdAndTenantId(albumId, tenantId);
        album.setPhotoCount(count.intValue());
        albumRepository.save(album);

        return saved;
    }

    @Transactional
    public void removePhoto(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new NoSuchElementException("Photo not found: " + photoId));
        String tenantId = TenantContext.getCurrentTenant();
        UUID albumId = photo.getAlbumId();

        photoRepository.deleteById(photoId);

        // Update album photo count
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found: " + albumId));
        Long count = photoRepository.countByAlbumIdAndTenantId(albumId, tenantId);
        album.setPhotoCount(count.intValue());
        albumRepository.save(album);

        log.info("Removed photo: {} from album: {}", photoId, albumId);
    }

    @Transactional(readOnly = true)
    public List<Photo> getAlbumPhotos(UUID albumId) {
        String tenantId = TenantContext.getCurrentTenant();
        return photoRepository.findByAlbumIdAndTenantId(albumId, tenantId);
    }
}
