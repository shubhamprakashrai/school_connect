package com.schoolmgmt.repository;

import com.schoolmgmt.model.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    Page<Assignment> findByTenantId(String tenantId, Pageable pageable);

    List<Assignment> findByClassIdAndTenantId(UUID classId, String tenantId);

    List<Assignment> findByTeacherIdAndTenantId(UUID teacherId, String tenantId);

    List<Assignment> findBySubjectIdAndTenantId(UUID subjectId, String tenantId);

    List<Assignment> findByStatusAndTenantId(Assignment.AssignmentStatus status, String tenantId);

    Page<Assignment> findByTenantIdAndClassId(String tenantId, UUID classId, Pageable pageable);

    Page<Assignment> findByTenantIdAndTeacherId(String tenantId, UUID teacherId, Pageable pageable);

    Page<Assignment> findByTenantIdAndSubjectId(String tenantId, UUID subjectId, Pageable pageable);

    Page<Assignment> findByTenantIdAndStatus(String tenantId, Assignment.AssignmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Assignment a WHERE a.tenantId = :tenantId " +
           "AND (:classId IS NULL OR a.classId = :classId) " +
           "AND (:teacherId IS NULL OR a.teacherId = :teacherId) " +
           "AND (:subjectId IS NULL OR a.subjectId = :subjectId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:type IS NULL OR a.type = :type)")
    Page<Assignment> findByFilters(
            @Param("tenantId") String tenantId,
            @Param("classId") UUID classId,
            @Param("teacherId") UUID teacherId,
            @Param("subjectId") UUID subjectId,
            @Param("status") Assignment.AssignmentStatus status,
            @Param("type") Assignment.AssignmentType type,
            Pageable pageable);
}
