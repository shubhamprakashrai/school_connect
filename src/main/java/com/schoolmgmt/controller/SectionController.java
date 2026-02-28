package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.CreateSectionRequest;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectionService.createSection(request));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<SectionResponse>> getByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(sectionService.getSectionsByClass(classId));
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<SectionResponse> update(@PathVariable UUID sectionId,
                                                  @Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, request));
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }


    /**
     * GET SECTION BY ID
     */
    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable UUID sectionId) {
        SectionResponse response = sectionService.getSectionById(sectionId);
        return ResponseEntity.ok(response);
    }


    /** Fetch sections by section name */
    /**
     * Fetch sections by section name
     */
    @GetMapping("/sections/by-name/{sectionName}")
    public ResponseEntity<List<SectionResponse>> getSectionsByName(@PathVariable String sectionName) {
        List<SectionResponse> responses = sectionService.getSectionsBySectionName(sectionName);
        return ResponseEntity.ok(responses);
    }
}