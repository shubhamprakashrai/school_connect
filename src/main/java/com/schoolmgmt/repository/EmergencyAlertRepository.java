package com.schoolmgmt.repository;

import com.schoolmgmt.model.EmergencyAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, UUID> {

    Page<EmergencyAlert> findByTenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);

    List<EmergencyAlert> findByTenantIdAndIsActiveAndIsDeletedFalse(String tenantId, Boolean isActive);

    List<EmergencyAlert> findByTenantIdAndAlertTypeAndIsDeletedFalse(String tenantId, EmergencyAlert.AlertType alertType);

    long countByTenantIdAndIsActiveAndIsDeletedFalse(String tenantId, Boolean isActive);
}
