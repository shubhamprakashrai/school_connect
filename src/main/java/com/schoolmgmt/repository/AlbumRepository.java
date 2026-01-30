package com.schoolmgmt.repository;

import com.schoolmgmt.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {

    Page<Album> findByTenantId(String tenantId, Pageable pageable);

    List<Album> findByIsPublishedTrueAndTenantIdOrderByCreatedAtDesc(String tenantId);

    List<Album> findByCategoryAndTenantId(String category, String tenantId);
}
