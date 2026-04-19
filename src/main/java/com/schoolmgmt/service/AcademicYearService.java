package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.AcademicYearRequest;
import com.schoolmgmt.dto.response.AcademicYearResponse;
//import com.schoolmgmt.exception.BusinessException;
//import com.schoolmgmt.exception.InternalServiceException;
//import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.repository.*;
//import com.schoolmgmt.repository.AcademicYearRepository;
import com.schoolmgmt.repository.*;
import  com.schoolmgmt.exception.*;
import com.schoolmgmt.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing academic years.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    /**
     * Creates a new academic year.
     *
     * @param request the academic year request
     * @return the created academic year response
     */
    @Transactional
    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Creating academic year: {} for tenant: {}", request.getName(), tenantId);

            // Check if academic year with same name already exists for this tenant
            if (academicYearRepository.findByTenantIdAndName(tenantId, request.getName()).isPresent()) {
                throw new BusinessException("Academic year with name '" + request.getName() + "' already exists");
            }

            // Validate dates
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BusinessException("End date must be after start date");
            }

            AcademicYear academicYear = AcademicYear.builder()
                    .name(request.getName())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.FALSE)
                    .build();
            academicYear.setTenantId(tenantId);

            AcademicYear saved = academicYearRepository.save(academicYear);
            log.info("Academic year created successfully: {}", saved.getId());

            return toResponse(saved);

        } catch (BusinessException e) {
            log.warn("Business exception while creating academic year: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating academic year: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while creating academic year", e);
        }
    }

    /**
     * Gets all academic years for current tenant.
     *
     * @return list of academic year responses
     */
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAllAcademicYears() {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching all academic years for tenant: {}", tenantId);

            List<AcademicYear> academicYears = academicYearRepository.findByTenantId(tenantId);

            return academicYears.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching academic years: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching academic years", e);
        }
    }

    /**
     * Gets academic year by ID.
     *
     * @param id the academic year ID
     * @return the academic year response
     */
    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching academic year: {} for tenant: {}", id, tenantId);

            AcademicYear academicYear = academicYearRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

            return toResponse(academicYear);

        } catch (ResourceNotFoundException e) {
            log.warn("Academic year not found: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching academic year {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching academic year", e);
        }
    }

    /**
     * Gets the currently active academic year.
     *
     * @return the active academic year response
     */
    @Transactional(readOnly = true)
    public AcademicYearResponse getActiveAcademicYear() {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.debug("Fetching active academic year for tenant: {}", tenantId);

            AcademicYear academicYear = academicYearRepository.findActiveByTenantId(tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "status", "active"));

            return toResponse(academicYear);

        } catch (ResourceNotFoundException e) {
            log.warn("No active academic year found for tenant");
            throw e;
        } catch (Exception e) {
            log.error("Error fetching active academic year: {}", e.getMessage(), e);
            throw new InternalServiceException("Internal server error while fetching active academic year", e);
        }
    }

    /**
     * Updates an academic year.
     *
     * @param id the academic year ID
     * @param request the update request
     * @return the updated academic year response
     */
    @Transactional
    public AcademicYearResponse updateAcademicYear(UUID id, AcademicYearRequest request) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Updating academic year: {} for tenant: {}", id, tenantId);

            AcademicYear academicYear = academicYearRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

            // Check name uniqueness if changed
            if (!academicYear.getName().equals(request.getName())) {
                if (academicYearRepository.findByTenantIdAndName(tenantId, request.getName()).isPresent()) {
                    throw new BusinessException("Academic year with name '" + request.getName() + "' already exists");
                }
            }

            // Validate dates
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BusinessException("End date must be after start date");
            }

            academicYear.setName(request.getName());
            academicYear.setStartDate(request.getStartDate());
            academicYear.setEndDate(request.getEndDate());
            if (request.getIsActive() != null) {
                academicYear.setIsActive(request.getIsActive());
            }

            AcademicYear updated = academicYearRepository.save(academicYear);
            log.info("Academic year updated successfully: {}", updated.getId());

            return toResponse(updated);

        } catch (ResourceNotFoundException | BusinessException e) {
            log.warn("Exception while updating academic year: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating academic year {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while updating academic year", e);
        }
    }

    /**
     * Sets an academic year as active (deactivates others).
     *
     * @param id the academic year ID to activate
     * @return the activated academic year response
     */
    @Transactional
    public AcademicYearResponse setActiveAcademicYear(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Setting active academic year: {} for tenant: {}", id, tenantId);

            AcademicYear academicYear = academicYearRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

            // Deactivate all other academic years for this tenant
            List<AcademicYear> allYears = academicYearRepository.findByTenantId(tenantId).stream()
                    .filter(ay -> !ay.getId().equals(id))
                    .collect(Collectors.toList());

            for (AcademicYear ay : allYears) {
                ay.setIsActive(false);
            }
            academicYearRepository.saveAll(allYears);

            // Activate the selected one
            academicYear.setIsActive(true);
            AcademicYear updated = academicYearRepository.save(academicYear);

            log.info("Academic year {} set as active for tenant {}", id, tenantId);
            return toResponse(updated);

        } catch (ResourceNotFoundException e) {
            log.warn("Academic year not found for activation: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error setting active academic year {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while setting active academic year", e);
        }
    }

    /**
     * Deletes an academic year.
     *
     * @param id the academic year ID
     */
    @Transactional
    public void deleteAcademicYear(UUID id) {
        try {
            String tenantId = TenantContext.requireCurrentTenant();
            log.info("Deleting academic year: {} for tenant: {}", id, tenantId);

            AcademicYear academicYear = academicYearRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

            academicYearRepository.delete(academicYear);
            log.info("Academic year deleted successfully: {}", id);

        } catch (ResourceNotFoundException e) {
            log.warn("Academic year not found for deletion: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting academic year {}: {}", id, e.getMessage(), e);
            throw new InternalServiceException("Internal server error while deleting academic year", e);
        }
    }

    private AcademicYearResponse toResponse(AcademicYear academicYear) {
        return AcademicYearResponse.builder()
                .id(academicYear.getId())
                .name(academicYear.getName())
                .startDate(academicYear.getStartDate())
                .endDate(academicYear.getEndDate())
                .isActive(academicYear.getIsActive())
                .createdAt(academicYear.getCreatedAt())
                .updatedAt(academicYear.getUpdatedAt())
                .build();
    }
}
