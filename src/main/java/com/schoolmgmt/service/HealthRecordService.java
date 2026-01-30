package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.HealthRecord;
import com.schoolmgmt.repository.HealthRecordRepository;
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
public class HealthRecordService {

    private final HealthRecordRepository repository;

    public HealthRecord create(HealthRecord record) {
        String tenantId = TenantContext.requireCurrentTenant();
        record.setTenantId(tenantId);
        HealthRecord saved = repository.save(record);
        log.info("Health record created: {} for student: {}", saved.getId(), saved.getStudentId());
        return saved;
    }

    @Transactional(readOnly = true)
    public HealthRecord getById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        HealthRecord record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", "id", id));
        if (!record.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("HealthRecord", "id", id);
        }
        return record;
    }

    @Transactional(readOnly = true)
    public Page<HealthRecord> getAll(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<HealthRecord> getByStudentId(String studentId) {
        String tenantId = TenantContext.requireCurrentTenant();
        return repository.findByStudentIdAndTenantIdAndIsActiveTrue(studentId, tenantId);
    }

    public HealthRecord update(UUID id, HealthRecord updates) {
        HealthRecord existing = getById(id);
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getDoctorName() != null) existing.setDoctorName(updates.getDoctorName());
        if (updates.getHospitalName() != null) existing.setHospitalName(updates.getHospitalName());
        if (updates.getDiagnosis() != null) existing.setDiagnosis(updates.getDiagnosis());
        if (updates.getPrescription() != null) existing.setPrescription(updates.getPrescription());
        if (updates.getSeverity() != null) existing.setSeverity(updates.getSeverity());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getNextFollowupDate() != null) existing.setNextFollowupDate(updates.getNextFollowupDate());
        return repository.save(existing);
    }

    public void delete(UUID id) {
        HealthRecord record = getById(id);
        record.softDelete(TenantContext.requireCurrentTenant());
        repository.save(record);
        log.info("Health record deleted: {}", id);
    }
}
