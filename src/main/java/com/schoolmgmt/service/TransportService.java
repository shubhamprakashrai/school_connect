package com.schoolmgmt.service;

import com.schoolmgmt.model.TransportRoute;
import com.schoolmgmt.repository.TransportRouteRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportService {

    private final TransportRouteRepository transportRouteRepository;

    @Transactional
    public TransportRoute createRoute(TransportRoute route) {
        String tenantId = TenantContext.getCurrentTenant();
        route.setTenantId(tenantId);
        log.info("Creating transport route: {} for tenant: {}", route.getRouteName(), tenantId);
        return transportRouteRepository.save(route);
    }

    @Transactional
    public TransportRoute updateRoute(UUID routeId, TransportRoute updated) {
        TransportRoute existing = transportRouteRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException("Transport route not found: " + routeId));

        if (updated.getRouteName() != null) existing.setRouteName(updated.getRouteName());
        if (updated.getRouteNumber() != null) existing.setRouteNumber(updated.getRouteNumber());
        if (updated.getStartPoint() != null) existing.setStartPoint(updated.getStartPoint());
        if (updated.getEndPoint() != null) existing.setEndPoint(updated.getEndPoint());
        if (updated.getStops() != null) existing.setStops(updated.getStops());
        if (updated.getDriverName() != null) existing.setDriverName(updated.getDriverName());
        if (updated.getDriverPhone() != null) existing.setDriverPhone(updated.getDriverPhone());
        if (updated.getVehicleNumber() != null) existing.setVehicleNumber(updated.getVehicleNumber());
        if (updated.getVehicleType() != null) existing.setVehicleType(updated.getVehicleType());
        if (updated.getCapacity() != null) existing.setCapacity(updated.getCapacity());
        if (updated.getCurrentStudentCount() != null) existing.setCurrentStudentCount(updated.getCurrentStudentCount());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return transportRouteRepository.save(existing);
    }

    @Transactional
    public void deleteRoute(UUID routeId) {
        transportRouteRepository.deleteById(routeId);
    }

    @Transactional(readOnly = true)
    public Page<TransportRoute> getAllRoutes(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return transportRouteRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public TransportRoute getRouteById(UUID routeId) {
        return transportRouteRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException("Transport route not found: " + routeId));
    }

    @Transactional(readOnly = true)
    public List<TransportRoute> getActiveRoutes() {
        String tenantId = TenantContext.getCurrentTenant();
        return transportRouteRepository.findByIsActiveAndTenantId(true, tenantId);
    }
}
