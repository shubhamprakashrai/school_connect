package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.BulkPromoteStudentsRequest.StudentPromotionEntry;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.AcademicYear;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.model.TeacherClass;
import com.schoolmgmt.repository.AcademicYearRepository;
import com.schoolmgmt.repository.StudentRepository;
import com.schoolmgmt.repository.TeacherClassRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for academic session migration and student promotion operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionMigrationService {

    private final AcademicYearService academicYearService;
    private final StudentRepository studentRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final AcademicYearRepository academicYearRepository;

    /**
     * Promote a batch of students to new classes/sections.
     * Does NOT create a new academic year — that is a separate step.
     *
     * @param promotionEntries list of promotion details per student
     * @return summary map with promoted, detained, and failed counts
     */
    @Transactional
    public Map<String, Object> promoteStudents(List<StudentPromotionEntry> promotionEntries) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Starting student promotion for {} students in tenant: {}", promotionEntries.size(), tenantId);

        int promotedCount = 0;
        int detainedCount = 0;
        int failedCount = 0;
        List<Map<String, String>> failures = new ArrayList<>();

        for (StudentPromotionEntry entry : promotionEntries) {
            try {
                Optional<Student> studentOpt = studentRepository.findById(entry.getStudentId());

                if (studentOpt.isEmpty()) {
                    failedCount++;
                    failures.add(Map.of(
                            "studentId", entry.getStudentId().toString(),
                            "reason", "Student not found"
                    ));
                    log.warn("Student not found: {}", entry.getStudentId());
                    continue;
                }

                Student student = studentOpt.get();

                // Verify student belongs to the current tenant
                if (!tenantId.equals(student.getTenantId())) {
                    failedCount++;
                    failures.add(Map.of(
                            "studentId", entry.getStudentId().toString(),
                            "reason", "Student does not belong to current tenant"
                    ));
                    log.warn("Student {} does not belong to tenant {}", entry.getStudentId(), tenantId);
                    continue;
                }

                if ("PROMOTED".equalsIgnoreCase(entry.getPromotionStatus())) {
                    // Update class, section, and optionally roll number
                    if (entry.getNewClassId() != null && !entry.getNewClassId().isBlank()) {
                        student.setCurrentClassId(entry.getNewClassId());
                    }
                    if (entry.getNewSectionId() != null && !entry.getNewSectionId().isBlank()) {
                        student.setCurrentSectionId(UUID.fromString(entry.getNewSectionId()));
                    }
                    if (entry.getNewRollNumber() != null && !entry.getNewRollNumber().isBlank()) {
                        student.setRollNumber(entry.getNewRollNumber());
                    }
                    studentRepository.save(student);
                    promotedCount++;
                    log.debug("Student {} promoted to class {} section {}",
                            student.getId(), entry.getNewClassId(), entry.getNewSectionId());

                } else if ("DETAINED".equalsIgnoreCase(entry.getPromotionStatus())) {
                    // Detained: keep same class, optionally move section
                    if (entry.getNewSectionId() != null && !entry.getNewSectionId().isBlank()) {
                        student.setCurrentSectionId(UUID.fromString(entry.getNewSectionId()));
                    }
                    if (entry.getNewRollNumber() != null && !entry.getNewRollNumber().isBlank()) {
                        student.setRollNumber(entry.getNewRollNumber());
                    }
                    studentRepository.save(student);
                    detainedCount++;
                    log.debug("Student {} detained in class {}", student.getId(), student.getCurrentClassId());

                } else {
                    failedCount++;
                    failures.add(Map.of(
                            "studentId", entry.getStudentId().toString(),
                            "reason", "Invalid promotion status: " + entry.getPromotionStatus()
                    ));
                }

            } catch (Exception e) {
                failedCount++;
                failures.add(Map.of(
                        "studentId", entry.getStudentId().toString(),
                        "reason", e.getMessage() != null ? e.getMessage() : "Unknown error"
                ));
                log.error("Failed to promote student {}: {}", entry.getStudentId(), e.getMessage(), e);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProcessed", promotionEntries.size());
        summary.put("promoted", promotedCount);
        summary.put("detained", detainedCount);
        summary.put("failed", failedCount);
        if (!failures.isEmpty()) {
            summary.put("failures", failures);
        }

        log.info("Student promotion completed - promoted: {}, detained: {}, failed: {}",
                promotedCount, detainedCount, failedCount);

        return summary;
    }

    /**
     * Full session migration:
     * 1. Create new academic year
     * 2. Deactivate old TeacherClass assignments for the current academic year
     * 3. Activate the new academic year (auto-deactivates the current one)
     *
     * @return migration summary
     */
    @Transactional
    public Map<String, Object> migrateSession(String newYearName, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Starting session migration to '{}' for tenant: {}", newYearName, tenantId);

        // 1. Create new academic year
        AcademicYear newYear = academicYearService.createAcademicYear(newYearName, startDate, endDate);
        log.info("Created new academic year: {} ({})", newYear.getName(), newYear.getId());

        // 2. Deactivate all active TeacherClass assignments for the current academic year
        int deactivatedAssignments = 0;
        Optional<AcademicYear> currentActiveYear = academicYearRepository.findActiveByTenantId(tenantId);
        if (currentActiveYear.isPresent()) {
            UUID currentYearId = currentActiveYear.get().getId();
            List<TeacherClass> activeAssignments =
                    teacherClassRepository.findByAcademicYearIdAndIsActiveTrueAndTenantId(currentYearId, tenantId);

            for (TeacherClass assignment : activeAssignments) {
                assignment.deactivate();
                teacherClassRepository.save(assignment);
                deactivatedAssignments++;
            }
            log.info("Deactivated {} teacher-class assignments for academic year: {}",
                    deactivatedAssignments, currentActiveYear.get().getName());
        } else {
            log.info("No currently active academic year found; skipping assignment deactivation");
        }

        // 3. Activate new academic year (this auto-deactivates the current one)
        academicYearService.activateAcademicYear(newYear.getId());
        log.info("Activated new academic year: {}", newYear.getName());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("newAcademicYearId", newYear.getId());
        summary.put("newAcademicYearName", newYear.getName());
        summary.put("deactivatedAssignments", deactivatedAssignments);
        summary.put("previousAcademicYear", currentActiveYear.map(AcademicYear::getName).orElse(null));

        log.info("Session migration completed successfully for tenant: {}", tenantId);

        return summary;
    }

    /**
     * Check the current migration status: active year info, student count, active assignments count.
     *
     * @return status map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMigrationStatus() {
        String tenantId = TenantContext.requireCurrentTenant();

        Map<String, Object> status = new LinkedHashMap<>();

        // Current active academic year
        Optional<AcademicYear> activeYear = academicYearRepository.findActiveByTenantId(tenantId);
        if (activeYear.isPresent()) {
            AcademicYear year = activeYear.get();
            Map<String, Object> yearInfo = new LinkedHashMap<>();
            yearInfo.put("id", year.getId());
            yearInfo.put("name", year.getName());
            yearInfo.put("startDate", year.getStartDate());
            yearInfo.put("endDate", year.getEndDate());
            status.put("activeAcademicYear", yearInfo);

            // Active teacher-class assignments for current year
            List<TeacherClass> activeAssignments =
                    teacherClassRepository.findByAcademicYearIdAndIsActiveTrueAndTenantId(year.getId(), tenantId);
            status.put("activeAssignmentsCount", activeAssignments.size());
        } else {
            status.put("activeAcademicYear", null);
            status.put("activeAssignmentsCount", 0);
        }

        // Active student count
        long activeStudentCount = studentRepository.countByTenantIdAndStatus(tenantId, Student.StudentStatus.ACTIVE);
        status.put("activeStudentCount", activeStudentCount);

        // All academic years count
        List<AcademicYear> allYears = academicYearRepository.findByTenantIdAndIsDeletedFalse(tenantId);
        status.put("totalAcademicYears", allYears.size());

        return status;
    }
}
