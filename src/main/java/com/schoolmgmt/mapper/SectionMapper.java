package com.schoolmgmt.mapper;

import com.schoolmgmt.dto.request.CreateSectionRequest;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import org.springframework.stereotype.Component;

@Component
public class SectionMapper {

    public Section toEntity(CreateSectionRequest request, SchoolClass schoolClass) {
        return Section.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .schoolClass(schoolClass)
                .build();
    }

    // UPDATED METHOD
    public SectionResponse toResponse(Section section, SchoolClass schoolClass) {
        return SectionResponse.builder()
                .id(section.getId())
                .name(section.getName())
                .capacity(section.getCapacity())
                .schoolClassId(schoolClass != null ? schoolClass.getId() : null)
                .schoolClassCode(schoolClass != null ? schoolClass.getClassIdentifier() : null)
                .schoolClassName(schoolClass != null ? schoolClass.getName() : null)
                .classTeacherId(section.getClassTeacher() != null ? section.getClassTeacher().getId() : null)
                .classTeacherName(section.getClassTeacher() != null
                        ? section.getClassTeacher().getFirstName() + " " + section.getClassTeacher().getLastName()
                        : null)
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}