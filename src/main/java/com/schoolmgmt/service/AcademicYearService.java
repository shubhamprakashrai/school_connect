package com.schoolmgmt.service;

import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.repository.AcademicYearRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for academic year management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    /**
     * Create a new academic year
     */
    public AcademicYear createAcademicYear(String name, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("End date must be after start date");
        }

        // Check name uniqueness for tenant
        Optional<AcademicYear> existing = academicYearRepository.findByTenantIdAndName(tenantId, name);
        if (existing.isPresent()) {
            throw new BusinessException("Academic year with name '" + name + "' already exists");
        }

        AcademicYear academicYear = AcademicYear.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(false)
                .build();

        academicYear.setTenantId(tenantId);

        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Academic year created: {} - {} in tenant: {}", saved.getId(), saved.getName(), tenantId);

        return saved;
    }

    /**
     * Get all academic years for the current tenant (non-deleted)
     */
    @Transactional(readOnly = true)
    public List<AcademicYear> getAllAcademicYears() {
        String tenantId = TenantContext.requireCurrentTenant();
        return academicYearRepository.findByTenantIdAndIsDeletedFalse(tenantId);
    }

    /**
     * Get academic year by ID for the current tenant
     */
    @Transactional(readOnly = true)
    public AcademicYear getAcademicYearById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        return academicYearRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));
    }

    /**
     * Get the currently active academic year for the current tenant
     */
    @Transactional(readOnly = true)
    public AcademicYear getActiveAcademicYear() {
        String tenantId = TenantContext.requireCurrentTenant();
        return academicYearRepository.findActiveByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("No active academic year found"));
    }

    /**
     * Activate an academic year. Only one can be active at a time per tenant.
     * Deactivates the current active year first, then activates the specified one.
     */
    public AcademicYear activateAcademicYear(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicYear academicYear = academicYearRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

        // Deactivate the currently active academic year if any
        Optional<AcademicYear> currentActive = academicYearRepository.findActiveByTenantId(tenantId);
        currentActive.ifPresent(active -> {
            active.setIsActive(false);
            academicYearRepository.save(active);
            log.info("Deactivated academic year: {} - {}", active.getId(), active.getName());
        });

        // Activate the requested academic year
        academicYear.setIsActive(true);
        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Activated academic year: {} - {} in tenant: {}", saved.getId(), saved.getName(), tenantId);

        return saved;
    }

    /**
     * Deactivate an academic year
     */
    public AcademicYear deactivateAcademicYear(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicYear academicYear = academicYearRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

        academicYear.setIsActive(false);
        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Deactivated academic year: {} - {}", saved.getId(), saved.getName());

        return saved;
    }

    /**
     * Soft delete an academic year
     */
    public void deleteAcademicYear(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();

        AcademicYear academicYear = academicYearRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

        academicYear.softDelete(tenantId);
        academicYearRepository.save(academicYear);

        log.info("Academic year soft deleted: {} - {}", id, academicYear.getName());
    }
}
