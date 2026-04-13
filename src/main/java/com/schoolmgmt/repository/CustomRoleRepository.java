package com.schoolmgmt.repository;

import com.schoolmgmt.model.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomRoleRepository extends JpaRepository<CustomRole, UUID> {

    List<CustomRole> findByTenantIdAndIsDeletedFalseOrderByNameAsc(String tenantId);

    Optional<CustomRole> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(String tenantId, String name);

    long countByTenantIdAndIsDeletedFalse(String tenantId);
}
