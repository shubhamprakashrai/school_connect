package com.schoolmgmt.mapper;

import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.model.SchoolClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SchoolClassMapper {

    private final SectionMapper sectionMapper;

    public SchoolClassMapper(SectionMapper sectionMapper) {
        this.sectionMapper = sectionMapper;
    }

    public SchoolClassResponse toResponse(SchoolClass entity) {
        // Safely handle null or empty sections
        List<SectionResponse> sectionResponses = entity.getSections() != null
                ? entity.getSections().stream()
                .map(section -> sectionMapper.toResponse(section,entity))
                .collect(Collectors.toList())
                : List.of();

        return SchoolClassResponse.builder()
                .id(entity.getId())
                .code(entity.getClassIdentifier())
                .name(entity.getName())
                .description(entity.getDescription())
                .sections(sectionResponses)  // now properly mapped
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}