package com.schoolmgmt.repository;

import com.schoolmgmt.model.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByIdAndTenantId(UUID id, String tenantId);
    Page<FileEntity> findByTenantId(String tenantId, Pageable pageable);
    List<FileEntity> findByTenantIdAndEntityTypeAndEntityId(String tenantId, String entityType, String entityId);
    void deleteByIdAndTenantId(UUID id, String tenantId);
}
