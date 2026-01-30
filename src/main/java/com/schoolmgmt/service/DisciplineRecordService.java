package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.DisciplineRecord;
import com.schoolmgmt.repository.DisciplineRecordRepository;
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
public class DisciplineRecordService {

    private final DisciplineRecordRepository repository;

    public DisciplineRecord create(DisciplineRecord record) {
        String tenantId = TenantContext.requireCurrentTenant();
        record.setTenantId(tenantId);
        DisciplineRecord saved = repository.save(record);
        log.info("Discipline record created: {} for student: {}", saved.getId(), saved.getStudentId());
        return saved;
    }

    @Transactional(readOnly = true)
    public DisciplineRecord getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        DisciplineRecord record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisciplineRecord", "id", id));
        if (!record.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("DisciplineRecord", "id", id);
        }
        return record;
    }

    @Transactional(readOnly = true)
    public Page<DisciplineRecord> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<DisciplineRecord> getByStudentId(String studentId) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByStudentIdAndTenantIdAndIsActiveTrue(studentId, tenantId);
    }

    public DisciplineRecord update(UUID id, DisciplineRecord updates) {
        DisciplineRecord existing = getById(id);
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getActionTaken() != null) existing.setActionTaken(updates.getActionTaken());
        if (updates.getSeverity() != null) existing.setSeverity(updates.getSeverity());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getResolutionDate() != null) existing.setResolutionDate(updates.getResolutionDate());
        if (updates.getParentNotified() != null) existing.setParentNotified(updates.getParentNotified());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        DisciplineRecord record = getById(id);
        record.softDelete(TenantContext.requireCurrentTenant());
        repository.save(record);
        log.info("Discipline record deleted: {}", id);
    }
}
