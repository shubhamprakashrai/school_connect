package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.PerformanceReview;
import com.schoolmgmt.repository.PerformanceReviewRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerformanceReviewService {

    private final PerformanceReviewRepository repository;

    public PerformanceReview create(PerformanceReview review) {
        String tenantId = TenantContext.requireCurrentTenant();
        review.setTenantId(tenantId);
        PerformanceReview saved = repository.save(review);
        log.info("Performance review created: {} for staff: {}", saved.getId(), saved.getStaffId());
        return saved;
    }

    @Transactional(readOnly = true)
    public PerformanceReview getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        PerformanceReview review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", "id", id));
        if (!review.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("PerformanceReview", "id", id);
        }
        return review;
    }

    @Transactional(readOnly = true)
    public Page<PerformanceReview> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getByStaffId(String staffId) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByStaffIdAndTenantIdAndIsActiveTrue(staffId, tenantId);
    }

    public PerformanceReview update(UUID id, PerformanceReview updates) {
        PerformanceReview existing = getById(id);
        if (updates.getOverallRating() != null) existing.setOverallRating(updates.getOverallRating());
        if (updates.getTeachingRating() != null) existing.setTeachingRating(updates.getTeachingRating());
        if (updates.getCommunicationRating() != null) existing.setCommunicationRating(updates.getCommunicationRating());
        if (updates.getPunctualityRating() != null) existing.setPunctualityRating(updates.getPunctualityRating());
        if (updates.getProfessionalDevelopmentRating() != null) existing.setProfessionalDevelopmentRating(updates.getProfessionalDevelopmentRating());
        if (updates.getStrengths() != null) existing.setStrengths(updates.getStrengths());
        if (updates.getAreasForImprovement() != null) existing.setAreasForImprovement(updates.getAreasForImprovement());
        if (updates.getGoals() != null) existing.setGoals(updates.getGoals());
        if (updates.getComments() != null) existing.setComments(updates.getComments());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        PerformanceReview review = getById(id);
        review.softDelete(TenantContext.requireCurrentTenant());
        repository.save(review);
        log.info("Performance review deleted: {}", id);
    }
}
