package com.schoolmgmt.repository;

import com.schoolmgmt.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findByAlbumIdAndTenantId(UUID albumId, String tenantId);

    Long countByAlbumIdAndTenantId(UUID albumId, String tenantId);
}
