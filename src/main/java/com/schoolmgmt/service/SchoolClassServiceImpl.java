package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
import com.schoolmgmt.dto.request.UpdateSchoolClassRequest;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.exception.DuplicateResourceException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.mapper.SchoolClassMapper;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.repository.SchoolClassRepository;
import com.schoolmgmt.repository.SectionRepository;
import com.schoolmgmt.service.SchoolClassService;
import com.schoolmgmt.util.TenantContext;
import com.schoolmgmt.util.UserIdGeneratorBasedonTenantIdentifies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final SectionRepository sectionRepository;

    @Override
    public SchoolClassResponse createClass(CreateSchoolClassRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating school class '{}' for tenant {}", request.getName(), tenantId);

        // Validate duplicate
        if (schoolClassRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
            throw new DuplicateResourceException(
                    "Class name '" + request.getName() + "' already exists for this tenant."
            );
        }

        // Get last sequence
        Integer lastSequence = Optional
                .ofNullable(schoolClassRepository.findMaxSequenceForTenant(tenantId))
                .orElse(0);

        // Generate identifier
        String classIdentifier = UserIdGeneratorBasedonTenantIdentifies
                .generateNextCode(tenantId + "CL", lastSequence, 2);

        log.info("Generated classIdentifier '{}' for tenant {}", classIdentifier, tenantId);

        // Create entity
        SchoolClass schoolClass = SchoolClass.builder()
                .classIdentifier(classIdentifier)
                .name(request.getName())
                .description(request.getDescription() == null ? "" : request.getDescription())
                .sections(new HashSet<>()) // ensure sections set is initialized
                .build();

        schoolClass.setCreatedBy("system");
        schoolClass.setUpdatedBy("system");

        // Create default section "A"
        Section defaultSection = Section.builder()
                .name("A")
                .capacity(50)
                .schoolClass(schoolClass)
                .build();

        // Add section to class
        schoolClass.getSections().add(defaultSection);

        // Save class (sections will be saved automatically due to cascade = ALL)
        schoolClass = schoolClassRepository.save(schoolClass);

        // Return response including sections
        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    public Page<SchoolClassResponse> getAllClasses(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();

        return schoolClassRepository
                .findByTenantId(tenantId, pageable)
                .map(schoolClassMapper::toResponse);
    }

    @Override
    public SchoolClassResponse getClassById(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (!tenantId.equals(schoolClass.getTenantId())) {
            throw new ResourceNotFoundException("Class not found for tenant");
        }

        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    public SchoolClassResponse updateClass(UUID classId, UpdateSchoolClassRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (!tenantId.equals(schoolClass.getTenantId())) {
            throw new ResourceNotFoundException("Class not found for tenant");
        }

        if (request.getName() != null) schoolClass.setName(request.getName());
        if (request.getDescription() != null) schoolClass.setDescription(request.getDescription());

        schoolClass.setUpdatedBy("system");

        return schoolClassMapper.toResponse(schoolClassRepository.save(schoolClass));
    }

    @Override
    public void deleteClass(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (!tenantId.equals(schoolClass.getTenantId())) {
            throw new ResourceNotFoundException("Class not found for tenant");
        }

        schoolClassRepository.delete(schoolClass);
    }

    @Override
    public List<SchoolClassResponse> createClassesBulk(List<CreateSchoolClassRequest> requests) {
        String tenantId = TenantContext.getCurrentTenant();

        Integer maxSuffix = schoolClassRepository.findMaxSequenceForTenant(tenantId);
        int sequence = maxSuffix == null ? 1 : maxSuffix + 1;

        List<SchoolClassResponse> responses = new ArrayList<>();

        for (CreateSchoolClassRequest request : requests) {

            if (sequence > 99) {
                throw new IllegalArgumentException("Maximum limit of 99 classes reached for tenant: " + tenantId);
            }

            if (schoolClassRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
                throw new DuplicateResourceException(
                        "Class name '" + request.getName() + "' already exists for tenant."
                );
            }

            String identifier = tenantId + String.format("%02d", sequence++);

            SchoolClass schoolClass = SchoolClass.builder()
                    .classIdentifier(identifier)
                    .name(request.getName())
                    .description(request.getDescription() == null ? "" : request.getDescription())
//                    .tenantId(tenantId)
                    .build();

            schoolClass.setCreatedBy("system");
            schoolClass.setUpdatedBy("system");

            schoolClass = schoolClassRepository.save(schoolClass);
            responses.add(schoolClassMapper.toResponse(schoolClass));
        }

        return responses;
    }

}