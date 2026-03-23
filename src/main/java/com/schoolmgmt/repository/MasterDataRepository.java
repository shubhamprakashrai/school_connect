package com.schoolmgmt.repository;

import com.schoolmgmt.model.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, UUID> {

    List<MasterData> findByTenantIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(String tenantId, MasterData.Category category);

    List<MasterData> findByTenantIdAndIsActiveTrueOrderByCategoryAscDisplayOrderAsc(String tenantId);

    List<MasterData> findByTenantIdAndCategory(String tenantId, MasterData.Category category);

    boolean existsByTenantIdAndCategoryAndValue(String tenantId, MasterData.Category category, String value);

    long countByTenantIdAndCategory(String tenantId, MasterData.Category category);
}
