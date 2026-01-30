package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Complaint;
import com.schoolmgmt.repository.ComplaintRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComplaintService {

    private final ComplaintRepository repository;

    public Complaint create(Complaint complaint) {
        String tenantId = TenantContext.requireCurrentTenant();
        complaint.setTenantId(tenantId);
        Complaint saved = repository.save(complaint);
        log.info("Complaint created: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Complaint getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        Complaint complaint = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));
        if (!complaint.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Complaint", "id", id);
        }
        return complaint;
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getByStatus(String status, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByStatusAndTenantIdAndIsActiveTrue(status, tenantId, pageable);
    }

    public Complaint update(UUID id, Complaint updates) {
        Complaint existing = getById(id);
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getAssignedToId() != null) existing.setAssignedToId(updates.getAssignedToId());
        if (updates.getAssignedToName() != null) existing.setAssignedToName(updates.getAssignedToName());
        if (updates.getResolution() != null) existing.setResolution(updates.getResolution());
        if (updates.getResolvedDate() != null) existing.setResolvedDate(updates.getResolvedDate());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        Complaint complaint = getById(id);
        complaint.softDelete(TenantContext.requireCurrentTenant());
        repository.save(complaint);
        log.info("Complaint deleted: {}", id);
    }
}
