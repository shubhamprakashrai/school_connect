package com.schoolmgmt.repository;

import com.schoolmgmt.model.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSettingsRepository extends JpaRepository<TenantSettings, UUID> {

    Optional<TenantSettings> findByTenantId(String tenantId);

    boolean existsByTenantId(String tenantId);

    @Query("SELECT ts FROM TenantSettings ts WHERE ts.tenantId = :tenantId")
    Optional<TenantSettings> getSettingsByTenantId(@Param("tenantId") String tenantId);

    void deleteByTenantId(String tenantId);
}
