package com.schoolmgmt.repository;

import com.schoolmgmt.model.TransportRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransportRouteRepository extends JpaRepository<TransportRoute, UUID> {

    Page<TransportRoute> findByTenantId(String tenantId, Pageable pageable);

    List<TransportRoute> findByIsActiveAndTenantId(Boolean isActive, String tenantId);

    List<TransportRoute> findByRouteNameContainingIgnoreCaseAndTenantId(String name, String tenantId);
}
