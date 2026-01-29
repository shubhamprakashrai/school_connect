package com.schoolmgmt.repository;

import com.schoolmgmt.model.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    List<AssignmentSubmission> findByAssignmentIdAndTenantId(UUID assignmentId, String tenantId);

    List<AssignmentSubmission> findByStudentIdAndTenantId(UUID studentId, String tenantId);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    Page<AssignmentSubmission> findByAssignmentId(UUID assignmentId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.tenantId = :tenantId")
    Long countByAssignmentIdAndTenantId(@Param("assignmentId") UUID assignmentId, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.tenantId = :tenantId AND s.status = 'SUBMITTED'")
    Long countSubmittedByAssignmentIdAndTenantId(@Param("assignmentId") UUID assignmentId, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.tenantId = :tenantId AND s.status = 'GRADED'")
    Long countGradedByAssignmentIdAndTenantId(@Param("assignmentId") UUID assignmentId, @Param("tenantId") String tenantId);

    @Query("SELECT AVG(s.marksObtained) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.tenantId = :tenantId AND s.status = 'GRADED'")
    Double getAverageMarksByAssignmentId(@Param("assignmentId") UUID assignmentId, @Param("tenantId") String tenantId);
}
