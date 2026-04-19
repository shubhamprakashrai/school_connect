package com.schoolmgmt.service;

import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Section;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.repository.SectionRepository;
import com.schoolmgmt.repository.TeacherRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing class-teacher assignments and section operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassService {

    private final SectionRepository sectionRepository;
    private final TeacherRepository teacherRepository;
    private final StudentService studentService;

    /**
     * Assign a teacher to a section as class teacher
     */
    public Section assignTeacherToSection(UUID teacherId, UUID sectionId) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Get teacher
        Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        // Get section
        Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + sectionId));

        // Assign teacher to section
        section.setClassTeacher(teacher);
        Section savedSection = sectionRepository.save(section);

        log.info("Assigned teacher {} ({}) to section {} ({})", 
                teacher.getFullName(), teacherId, 
                section.getName(), sectionId);

        return savedSection;
    }

    /**
     * Remove teacher assignment from a section
     */
    public Section removeTeacherFromSection(UUID sectionId) {
        String tenantId = TenantContext.requireCurrentTenant();

        Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + sectionId));

        Teacher previousTeacher = section.getClassTeacher();
        section.setClassTeacher(null);
        Section savedSection = sectionRepository.save(section);

        log.info("Removed teacher {} from section {}", 
                previousTeacher != null ? previousTeacher.getFullName() : "None", 
                section.getName());

        return savedSection;
    }

    /**
     * Get all students in a section (for teacher access)
     */
    public List<StudentResponse> getStudentsInSection(UUID sectionId) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Verify section exists and belongs to tenant
        Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + sectionId));

        log.info("Fetching students for section: {} ({})", section.getName(), sectionId);
        return studentService.getStudentsBySection(sectionId);
    }

    /**
     * Get sections assigned to a teacher
     */
    public List<Section> getSectionsForTeacher(UUID teacherId) {
        String tenantId = TenantContext.requireCurrentTenant();

        // Verify teacher exists and belongs to tenant
        Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        List<Section> sections = sectionRepository.findByClassTeacherIdAndTenantId(teacherId, tenantId);
        
        log.info("Found {} sections assigned to teacher {}", String.valueOf(sections.size()), teacher.getFullName());
        return sections;
    }

    /**
     * Verify if a teacher is assigned to a specific section
     */
    public boolean isTeacherAssignedToSection(UUID teacherId, UUID sectionId) {
        String tenantId = TenantContext.requireCurrentTenant();

        Section section = sectionRepository.findByIdAndTenantId(sectionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + sectionId));

        return section.getClassTeacher() != null && 
               section.getClassTeacher().getId().equals(teacherId);
    }
}
