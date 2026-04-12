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
        if (route.getRouteNumber() == null || route.getRouteNumber().isBlank()) {
            route.setRouteNumber(generateNextRouteNumber(tenantId));
        }
        log.info("Creating transport route: {} ({}) for tenant: {}",
                route.getRouteName(), route.getRouteNumber(), tenantId);
        return transportRouteRepository.save(route);
    }

    /** Generates next available route number per tenant: R001, R002, … */
    private String generateNextRouteNumber(String tenantId) {
        long base = transportRouteRepository.countByTenantId(tenantId) + 1;
        String candidate = String.format("R%03d", base);
        // Skip collisions in case of gaps / concurrent inserts.
        while (transportRouteRepository.existsByTenantIdAndRouteNumber(tenantId, candidate)) {
            base++;
            candidate = String.format("R%03d", base);
        }
        return candidate;
    }

    @Transactional
    public TransportRoute updateRoute(UUID routeId, TransportRoute updated) {
        String tenantId = TenantContext.getCurrentTenant();
        TransportRoute existing = transportRouteRepository.findById(routeId)
                .filter(r -> tenantId.equals(r.getTenantId()))
                .orElseThrow(() -> new NoSuchElementException("Transport route not found: " + routeId));

        if (updated.getRouteName() != null) existing.setRouteName(updated.getRouteName());
        if (updated.getRouteNumber() != null) existing.setRouteNumber(updated.getRouteNumber());
        if (updated.getStartPoint() != null) existing.setStartPoint(updated.getStartPoint());
        if (updated.getEndPoint() != null) existing.setEndPoint(updated.getEndPoint());
        if (updated.getStartTime() != null) existing.setStartTime(updated.getStartTime());
        if (updated.getEndTime() != null) existing.setEndTime(updated.getEndTime());
        if (updated.getDistance() != null) existing.setDistance(updated.getDistance());
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
        String tenantId = TenantContext.getCurrentTenant();
        TransportRoute existing = transportRouteRepository.findById(routeId)
                .filter(r -> tenantId.equals(r.getTenantId()))
                .orElseThrow(() -> new NoSuchElementException("Transport route not found: " + routeId));
        transportRouteRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Page<TransportRoute> getAllRoutes(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return transportRouteRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public TransportRoute getRouteById(UUID routeId) {
        String tenantId = TenantContext.getCurrentTenant();
        return transportRouteRepository.findById(routeId)
                .filter(r -> tenantId.equals(r.getTenantId()))
                .orElseThrow(() -> new NoSuchElementException("Transport route not found: " + routeId));
    }

    @Transactional(readOnly = true)
    public List<TransportRoute> getActiveRoutes() {
        String tenantId = TenantContext.getCurrentTenant();
        return transportRouteRepository.findByIsActiveAndTenantId(true, tenantId);
    }
}
