package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
import com.schoolmgmt.dto.request.CreateSectionRequest;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.repository.SchoolClassRepository;
import com.schoolmgmt.repository.SectionRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassSectionService {
    
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    
    public SchoolClassResponse createSchoolClass(CreateSchoolClassRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        
        // Check if class code already exists for this tenant
        if (schoolClassRepository.findByTenantIdAndCode(UUID.fromString(tenantId), request.getCode()).isPresent()) {
            throw new RuntimeException("Class with code '" + request.getCode() + "' already exists");
        }
        
        // Create school class
        SchoolClass schoolClass = SchoolClass.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        schoolClass.setTenantId(tenantId);
        schoolClass.setCreatedBy("system");
        schoolClass.setUpdatedBy("system");
        
        schoolClass = schoolClassRepository.save(schoolClass);
        
        // Create sections if provided
        List<SectionResponse> sectionResponses = null;
        if (request.getSections() != null && !request.getSections().isEmpty()) {
            sectionResponses = createSectionsForClass(schoolClass.getId(), request.getSections());
        } else {
            // Create default section if no sections specified
            sectionResponses = createDefaultSection(schoolClass.getId());
        }
        
        return SchoolClassResponse.builder()
                .id(schoolClass.getId())
                .code(schoolClass.getCode())
                .name(schoolClass.getName())
                .description(schoolClass.getDescription())
                .sections(sectionResponses)
                .createdAt(schoolClass.getCreatedAt())
                .updatedAt(schoolClass.getUpdatedAt())
                .build();
    }
    
    public SectionResponse createSection(CreateSectionRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        
        // Validate school class exists
        SchoolClass schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                .orElseThrow(() -> new RuntimeException("School class not found"));
        
        if (!schoolClass.getTenantId().equals(tenantId)) {
            throw new RuntimeException("School class not found in current tenant");
        }
        
        // Check if section already exists for this class
        if (sectionRepository.findByTenantIdAndSchoolClassIdAndName(
                UUID.fromString(tenantId), request.getSchoolClassId(), request.getName()).isPresent()) {
            throw new RuntimeException("Section '" + request.getName() + "' already exists for this class");
        }
        
        Section section = Section.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .schoolClassId(request.getSchoolClassId())
                .build();
        
        section.setTenantId(tenantId);
        section.setCreatedBy("system");
        section.setUpdatedBy("system");
        
        section = sectionRepository.save(section);
        
        return buildSectionResponse(section, schoolClass);
    }
    
    public List<SectionResponse> createSectionsForClass(UUID schoolClassId, List<CreateSectionRequest> sectionRequests) {
        return sectionRequests.stream()
                .map(sectionRequest -> {
                    sectionRequest.setSchoolClassId(schoolClassId);
                    return createSection(sectionRequest);
                })
                .collect(Collectors.toList());
    }
    
    public List<SectionResponse> createDefaultSection(UUID schoolClassId) {
        CreateSectionRequest defaultSectionRequest = CreateSectionRequest.builder()
                .name("A")
                .capacity(40)
                .schoolClassId(schoolClassId)
                .build();
        
        return List.of(createSection(defaultSectionRequest));
    }
    
    public Page<SchoolClassResponse> getAllClasses(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        List<SchoolClass> classes = schoolClassRepository.findAllByTenantId(UUID.fromString(tenantId));
        
        List<SchoolClassResponse> responses = classes.stream()
                .map(this::buildClassResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, responses.size());
    }
    
    public SchoolClassResponse getClassById(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("School class not found"));
        
        if (!schoolClass.getTenantId().equals(tenantId)) {
            throw new RuntimeException("School class not found in current tenant");
        }
        
        return buildClassResponse(schoolClass);
    }
    
    public List<SectionResponse> getSectionsByClassId(UUID classId) {
        String tenantId = TenantContext.getCurrentTenant();
        List<Section> sections = sectionRepository.findByTenantIdAndSchoolClassId(UUID.fromString(tenantId), classId);
        
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("School class not found"));
        
        return sections.stream()
                .map(section -> buildSectionResponse(section, schoolClass))
                .collect(Collectors.toList());
    }
    
    private SchoolClassResponse buildClassResponse(SchoolClass schoolClass) {
        List<SectionResponse> sections = getSectionsByClassId(schoolClass.getId());
        
        return SchoolClassResponse.builder()
                .id(schoolClass.getId())
                .code(schoolClass.getCode())
                .name(schoolClass.getName())
                .description(schoolClass.getDescription())
                .sections(sections)
                .createdAt(schoolClass.getCreatedAt())
                .updatedAt(schoolClass.getUpdatedAt())
                .build();
    }
    
    private SectionResponse buildSectionResponse(Section section, SchoolClass schoolClass) {
        return SectionResponse.builder()
                .id(section.getId())
                .name(section.getName())
                .capacity(section.getCapacity())
                .schoolClassId(section.getSchoolClassId())
                .schoolClassCode(schoolClass.getCode())
                .schoolClassName(schoolClass.getName())
                .classTeacherId(section.getClassTeacher() != null ? section.getClassTeacher().getId() : null)
                .classTeacherName(section.getClassTeacher() != null ? 
                    section.getClassTeacher().getFirstName() + " " + section.getClassTeacher().getLastName() : null)
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}