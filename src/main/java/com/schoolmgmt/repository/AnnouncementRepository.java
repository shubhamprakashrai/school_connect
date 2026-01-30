package com.schoolmgmt.repository;

import com.schoolmgmt.model.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    Page<Announcement> findByTenantId(String tenantId, Pageable pageable);

    Page<Announcement> findByIsPublishedTrueAndTenantId(String tenantId, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE :role MEMBER OF a.targetRoles AND a.isPublished = true AND a.tenantId = :tenantId AND (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.publishedAt DESC")
    List<Announcement> findByTargetRoleAndActive(@Param("role") String role, @Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    List<Announcement> findByIsPublishedTrueAndExpiresAtAfterAndTenantIdOrderByPublishedAtDesc(LocalDateTime now, String tenantId);

    List<Announcement> findByAuthorIdAndTenantId(UUID authorId, String tenantId);
}
