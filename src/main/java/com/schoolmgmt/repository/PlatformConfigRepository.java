package com.schoolmgmt.repository;

import com.schoolmgmt.model.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Platform Configuration operations.
 */
@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, UUID> {

    /**
     * Find config by key
     */
    Optional<PlatformConfig> findByConfigKey(String configKey);

    /**
     * Check if config key exists
     */
    boolean existsByConfigKey(String configKey);
}
