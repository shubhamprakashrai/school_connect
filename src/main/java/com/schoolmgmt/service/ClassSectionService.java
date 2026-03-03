//
//package com.schoolmgmt.service;
//
//import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
//import com.schoolmgmt.dto.request.CreateSectionRequest;
//import com.schoolmgmt.dto.response.SchoolClassResponse;
//import com.schoolmgmt.dto.response.SectionResponse;
//import com.schoolmgmt.model.SchoolClass;
//import com.schoolmgmt.model.Section;
//import com.schoolmgmt.repository.SchoolClassRepository;
//import com.schoolmgmt.repository.SectionRepository;
//import com.schoolmgmt.util.TenantContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class ClassSectionService {
//
//    private final SchoolClassRepository schoolClassRepository;
//    private final SectionRepository sectionRepository;
//
//    public SchoolClassResponse createSchoolClass(CreateSchoolClassRequest request) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//
//        // 1. Find max identifier for this tenant
//        Integer maxId = schoolClassRepository.findMaxSequenceForTenant(tenantId);
//
//        // 2. Generate next identifier (e.g., T01-01, T01-02)
//        String newIdentifier = tenantId  + String.format("%02d", (maxId == null ? 1 : maxId + 1));
//
//        // 3. Optional: set default description if null
//
//        String description = "";
//        if (request.getDescription() == null) {
//            description = ""; // or any default text like "No description"
//        }
//        else {
//            description = request.getDescription();
//        }
//
//        // 🔍 4. Check if class name already exists for this tenant
//        if (schoolClassRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
//            throw new IllegalArgumentException("Class name '" + request.getName() + "' Already exists for this tenant.");
//        }
//
//
//
//        // Create school class
//        SchoolClass schoolClass = SchoolClass.builder()
//                .code(newIdentifier)
//                .name(request.getName())
//                .description(description)
//                .build();
//
//        schoolClass.setTenantId(tenantId);
//        schoolClass.setCreatedBy("system");
//        schoolClass.setUpdatedBy("system");
//
//        schoolClass = schoolClassRepository.save(schoolClass);
//
//
//        // Create sections if provided
//        List<SectionResponse> sectionResponses = null;
//        if (request.getSections() != null && !request.getSections().isEmpty()) {
//            sectionResponses = createSectionsForClass(schoolClass.getId(), request.getSections());
//        } else {
//            // Create default section if no sections specified
//            sectionResponses = createDefaultSection(schoolClass.getId());
//        }
//
//        return SchoolClassResponse.builder()
//                .id(schoolClass.getId())
//                .code(schoolClass.getCode())
//                .name(schoolClass.getName())
//                .description(schoolClass.getDescription())
//                .sections(sectionResponses)
//                .createdAt(schoolClass.getCreatedAt())
//                .updatedAt(schoolClass.getUpdatedAt())
//                .build();
//    }
//
//
//
//
//    /**
//     * Bulk creation of school classes for the current tenant.
//     */
//    public List<SchoolClassResponse> createSchoolClasses(List<CreateSchoolClassRequest> requests) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        // 1. Fetch max suffix for this tenant (last 2 digits of school_identifier)
//        Integer maxSuffix = schoolClassRepository.findMaxSequenceForTenant(tenantId);
//        int sequence = (maxSuffix == null ? 1 : maxSuffix + 1);
//
//        List<SchoolClassResponse> responses = new ArrayList<>();
//
//        for (CreateSchoolClassRequest request : requests) {
//            // 2. Limit check (max 99 classes per tenant)
//            if (sequence > 99) {
//                throw new IllegalArgumentException("Maximum class limit reached for tenant: " + tenantId);
//            }
//
//            // 3. Generate classIdentifier (tenantId + 2-digit sequence)
//            String newIdentifier = tenantId + String.format("%02d", sequence++);
//
//            // 4. Optional description
//            String description = (request.getDescription() == null ? "" : request.getDescription());
//
//            // 5. Check duplicate class name for tenant
//            if (schoolClassRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
//                throw new IllegalArgumentException(
//                        "Class name '" + request.getName() + "' already exists for tenant: " + tenantId
//                );
//            }
//
//            // 6. Build entity
//            SchoolClass schoolClass = SchoolClass.builder()
//                    .code(newIdentifier)
//                    .name(request.getName())
//                    .description(description)
//                    .build();
//
//            schoolClass.setTenantId(tenantId);
//            schoolClass.setCreatedBy("system");
//            schoolClass.setUpdatedBy("system");
//
//            schoolClass = schoolClassRepository.save(schoolClass);
//
//            // 7. Handle sections
//            List<SectionResponse> sectionResponses;
//            if (request.getSections() != null && !request.getSections().isEmpty()) {
//                sectionResponses = createSectionsForClass(schoolClass.getId(), request.getSections());
//            } else {
//                sectionResponses = createDefaultSection(schoolClass.getId());
//            }
//
//            // 8. Build response
//            responses.add(SchoolClassResponse.builder()
//                    .id(schoolClass.getId())
//                    .code(schoolClass.getCode())
//                    .name(schoolClass.getName())
//                    .description(schoolClass.getDescription())
//                    .sections(sectionResponses)
//                    .createdAt(schoolClass.getCreatedAt())
//                    .updatedAt(schoolClass.getUpdatedAt())
//                    .build());
//        }
//
//        return responses;
//    }
//
//
//
//
//
//    public SectionResponse createSection(CreateSectionRequest request) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        // Validate school class exists
//        SchoolClass schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
//                .orElseThrow(() -> new RuntimeException("School class not found"));
//
//        if (!schoolClass.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("School class not found in current tenant");
//        }
//
//        // Check if section already exists for this class
//        if (sectionRepository.findByTenantIdAndSchoolClassIdAndName(
//                tenantId, request.getSchoolClassId(), request.getName()).isPresent()) {
//            throw new RuntimeException("Section '" + request.getName() + "' already exists for this class");
//        }
//
//        Section section = Section.builder()
//                .name(request.getName())
//                .capacity(request.getCapacity())
//                .schoolClass(schoolClass)
//                .build();
//
//        section.setTenantId(tenantId);
//        section.setCreatedBy("system");
//        section.setUpdatedBy("system");
//
//        section = sectionRepository.save(section);
//
//        return buildSectionResponse(section, schoolClass);
//    }
//
//    public List<SectionResponse> createSectionsForClass(UUID schoolClassId, List<CreateSectionRequest> sectionRequests) {
//        return sectionRequests.stream()
//                .map(sectionRequest -> {
//                    sectionRequest.setSchoolClassId(schoolClassId);
//                    return createSection(sectionRequest);
//                })
//                .collect(Collectors.toList());
//    }
//
//    public List<SectionResponse> createDefaultSection(UUID schoolClassId) {
//        CreateSectionRequest defaultSectionRequest = CreateSectionRequest.builder()
//                .name("A")
//                .capacity(60)
//                .schoolClassId(schoolClassId)
//                .build();
//
//        return List.of(createSection(defaultSectionRequest));
//    }
//
//    public Page<SchoolClassResponse> getAllClasses(Pageable pageable) {
//        String tenantId = TenantContext.getCurrentTenant();
//        List<SchoolClass> classes = schoolClassRepository.findAllByTenantId(tenantId);
//
//        List<SchoolClassResponse> responses = classes.stream()
//                .map(this::buildClassResponse)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(responses, pageable, responses.size());
//    }
//
//    public SchoolClassResponse getClassById(UUID classId) {
//        String tenantId = TenantContext.getCurrentTenant();
//        SchoolClass schoolClass = schoolClassRepository.findById(classId)
//                .orElseThrow(() -> new RuntimeException("School class not found"));
//
//        if (!schoolClass.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("School class not found in current tenant");
//        }
//
//        return buildClassResponse(schoolClass);
//    }
//
//    public List<SectionResponse> getSectionsByClassId(UUID classId) {
//        String tenantId = TenantContext.getCurrentTenant();
//        List<Section> sections = sectionRepository.findByTenantIdAndSchoolClassId(tenantId, classId);
//
//        SchoolClass schoolClass = schoolClassRepository.findById(classId)
//                .orElseThrow(() -> new RuntimeException("School class not found"));
//
//        return sections.stream()
//                .map(section -> buildSectionResponse(section, schoolClass))
//                .collect(Collectors.toList());
//    }
//
//    private SchoolClassResponse buildClassResponse(SchoolClass schoolClass) {
//        List<SectionResponse> sections = getSectionsByClassId(schoolClass.getId());
//
//        return SchoolClassResponse.builder()
//                .id(schoolClass.getId())
//                .code(schoolClass.getCode())
//                .name(schoolClass.getName())
//                .description(schoolClass.getDescription())
//                .sections(sections)
//                .createdAt(schoolClass.getCreatedAt())
//                .updatedAt(schoolClass.getUpdatedAt())
//                .build();
//    }
//
//    private SectionResponse buildSectionResponse(Section section, SchoolClass schoolClass) {
//        return SectionResponse.builder()
//                .id(section.getId())
//                .name(section.getName())
//                .capacity(section.getCapacity())
//                .schoolClassId(section.getSchoolClass().getId())
//                .schoolClassCode(schoolClass.getCode())
//                .schoolClassName(schoolClass.getName())
//                .classTeacherId(section.getClassTeacher() != null ? section.getClassTeacher().getId() : null)
//                .classTeacherName(section.getClassTeacher() != null ?
//                    section.getClassTeacher().getFirstName() + " " + section.getClassTeacher().getLastName() : null)
//                .createdAt(section.getCreatedAt())
//                .updatedAt(section.getUpdatedAt())
//                .build();
//    }
//
//
//
//    // Update class and return updated info
//    public SchoolClassResponse updateClass(UUID classId, CreateSchoolClassRequest request) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        SchoolClass schoolClass = schoolClassRepository.findById(classId)
//                .orElseThrow(() -> new RuntimeException("Class not found"));
//
//        if (!schoolClass.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("Class not found in current tenant");
//        }
//
//        // Update fields
//        schoolClass.setName(request.getName());
//        schoolClass.setDescription(request.getDescription());
//        schoolClass.setUpdatedBy("system");
//
//        SchoolClass updated = schoolClassRepository.save(schoolClass);
//
//        return buildClassResponse(updated);
//    }
//
//    // Update section and return updated info
//    public SectionResponse updateSection(UUID sectionId, CreateSectionRequest request) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        Section section = sectionRepository.findById(sectionId)
//                .orElseThrow(() -> new RuntimeException("Section not found"));
//
//        if (!section.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("Section not found in current tenant");
//        }
//
//        // Update fields
//        section.setName(request.getName());
//        section.setCapacity(request.getCapacity());
//        section.setUpdatedBy("system");
//
//        Section updated = sectionRepository.save(section);
//
//        SchoolClass schoolClass = schoolClassRepository.findById(updated.getSchoolClass().getId())
//                .orElseThrow(() -> new RuntimeException("Class not found"));
//
//        return buildSectionResponse(updated, schoolClass);
//    }
//
//
//
//    // Delete class and return deleted info
//    public SchoolClassResponse deleteClass(UUID classId) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        SchoolClass schoolClass = schoolClassRepository.findById(classId)
//                .orElseThrow(() -> new RuntimeException("Class not found"));
//
//        if (!schoolClass.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("Class not found in current tenant");
//        }
//
//        // Fetch sections before deletion
//        List<SectionResponse> sectionResponses = getSectionsByClassId(classId);
//
//        // Delete sections first
//        sectionRepository.deleteAllById(
//                sectionResponses.stream().map(SectionResponse::getId).toList()
//        );
//
//        // Delete class
//        schoolClassRepository.delete(schoolClass);
//
//        // Return info about deleted class
//        return SchoolClassResponse.builder()
//                .id(schoolClass.getId())
//                .code(schoolClass.getCode())
//                .name(schoolClass.getName())
//                .description(schoolClass.getDescription())
//                .sections(sectionResponses)
//                .createdAt(schoolClass.getCreatedAt())
//                .updatedAt(schoolClass.getUpdatedAt())
//                .build();
//    }
//
//    // Delete section and return deleted info
//    public SectionResponse deleteSection(UUID sectionId) {
//        String tenantId = TenantContext.getCurrentTenant();
//
//        Section section = sectionRepository.findById(sectionId)
//                .orElseThrow(() -> new RuntimeException("Section not found"));
//
//        if (!section.getTenantId().equals(tenantId)) {
//            throw new RuntimeException("Section not found in current tenant");
//        }
//
//        SchoolClass schoolClass = schoolClassRepository.findById(section.getSchoolClass().getId())
//                .orElseThrow(() -> new RuntimeException("Class not found"));
//
//        // Delete section
//        sectionRepository.delete(section);
//
//        // Return info about deleted section
//        return buildSectionResponse(section, schoolClass);
//    }
//
//}
//
