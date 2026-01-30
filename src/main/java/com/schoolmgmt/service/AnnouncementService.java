package com.schoolmgmt.service;

import com.schoolmgmt.model.Announcement;
import com.schoolmgmt.repository.AnnouncementRepository;
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
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional
    public Announcement create(Announcement announcement) {
        String tenantId = TenantContext.getCurrentTenant();
        announcement.setTenantId(tenantId);
        log.info("Creating announcement: {} for tenant: {}", announcement.getTitle(), tenantId);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public Announcement update(UUID id, Announcement updated) {
        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Announcement not found: " + id));

        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getContent() != null) existing.setContent(updated.getContent());
        if (updated.getPriority() != null) existing.setPriority(updated.getPriority());
        if (updated.getTargetRoles() != null) existing.setTargetRoles(updated.getTargetRoles());
        if (updated.getTargetClassIds() != null) existing.setTargetClassIds(updated.getTargetClassIds());
        if (updated.getExpiresAt() != null) existing.setExpiresAt(updated.getExpiresAt());

        return announcementRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        announcementRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getAll(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return announcementRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Announcement getById(UUID id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Announcement not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Announcement> getByRole(String role) {
        String tenantId = TenantContext.getCurrentTenant();
        return announcementRepository.findByTargetRoleAndActive(role, tenantId, LocalDateTime.now());
    }

    @Transactional
    public Announcement publish(UUID id) {
        Announcement announcement = getById(id);
        announcement.setIsPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    @Transactional
    public Announcement unpublish(UUID id) {
        Announcement announcement = getById(id);
        announcement.setIsPublished(false);
        return announcementRepository.save(announcement);
    }
}
