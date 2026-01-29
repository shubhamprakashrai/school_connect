package com.schoolmgmt.repository;

import com.schoolmgmt.model.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, UUID> {

    List<FeeType> findByTenantIdOrderByName(String tenantId);

    List<FeeType> findByTenantIdAndIsActiveTrue(String tenantId);

    boolean existsByTenantIdAndName(String tenantId, String name);
}
