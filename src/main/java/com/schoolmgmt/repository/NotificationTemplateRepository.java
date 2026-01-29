package com.schoolmgmt.repository;

import com.schoolmgmt.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    List<NotificationTemplate> findByTenantIdAndIsActiveTrue(String tenantId);

    List<NotificationTemplate> findByTenantIdAndTypeAndIsActiveTrue(String tenantId, String type);

    List<NotificationTemplate> findByTenantIdOrderByName(String tenantId);
}
