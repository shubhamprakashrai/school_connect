package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateSectionRequest;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.exception.ResourceAlreadyExistsException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.mapper.SectionMapper;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.repository.SchoolClassRepository;
import com.schoolmgmt.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionMapper sectionMapper;

    /** CREATE SECTION */
    public SectionResponse createSection(CreateSectionRequest request) {
        SchoolClass schoolClass = getSchoolClassOrThrow(request.getSchoolClassId());

        if (sectionRepository.existsBySchoolClassAndName(schoolClass, request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Section '" + request.getName() + "' already exists in class '" + schoolClass.getName() + "'"
            );
        }

        Section section = sectionMapper.toEntity(request, schoolClass);
        Section saved = sectionRepository.save(section);
        log.info("Created section '{}' in class '{}'", saved.getName(), schoolClass.getName());

        // Pass schoolClass to avoid LazyInitializationException
        return sectionMapper.toResponse(saved, schoolClass);
    }

    /** GET ALL SECTIONS OF A CLASS */
    public List<SectionResponse> getSectionsByClass(UUID classId) {
        SchoolClass schoolClass = getSchoolClassOrThrow(classId);

        return sectionRepository.findBySchoolClass(schoolClass)
                .stream()
                .map(section -> sectionMapper.toResponse(section, schoolClass))
                .collect(Collectors.toList());
    }

    /** UPDATE SECTION */
    public SectionResponse updateSection(UUID sectionId, CreateSectionRequest request) {
        Section section = getSectionOrThrow(sectionId);

        // Use the existing class of the section
        SchoolClass schoolClass = section.getSchoolClass();

        // Check if the new name already exists in the same class
        if (!section.getName().equals(request.getName()) &&
                sectionRepository.existsBySchoolClassAndName(schoolClass, request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Section '" + request.getName() + "' already exists in class '" + schoolClass.getName() + "'"
            );
        }

        section.setName(request.getName());
        section.setCapacity(request.getCapacity());

        Section updated = sectionRepository.save(section);
        log.info("Updated section '{}' in class '{}'", updated.getName(), schoolClass.getName());

        return sectionMapper.toResponse(updated, schoolClass);
    }

    /** DELETE SECTION */
    public void deleteSection(UUID sectionId) {
        Section section = getSectionOrThrow(sectionId);

        // Fetch schoolClass for logging before deletion
        SchoolClass schoolClass = section.getSchoolClass();

        sectionRepository.delete(section);
        log.info("Deleted section '{}' from class '{}'", section.getName(), schoolClass.getName());
    }

    // ----------------- HELPERS -----------------

    private SchoolClass getSchoolClassOrThrow(UUID classId) {
        return schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolClass not found: " + classId));
    }

    private Section getSectionOrThrow(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));
    }



    /** FETCH SECTION BY ID */
    public SectionResponse getSectionById(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        SchoolClass schoolClass = section.getSchoolClass(); // fetch linked class
        return sectionMapper.toResponse(section, schoolClass);
    }


    /** Fetch sections by section name */
    public List<SectionResponse> getSectionsBySectionName(String sectionName) {
        List<Section> sections = sectionRepository.findByName(sectionName);
        if (sections.isEmpty()) {
            throw new ResourceNotFoundException("No sections found with name: " + sectionName);
        }
        return sections.stream()
                .map(section -> sectionMapper.toResponse(section, section.getSchoolClass()))
                .collect(Collectors.toList());
    }


}