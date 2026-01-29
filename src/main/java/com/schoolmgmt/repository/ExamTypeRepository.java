package com.schoolmgmt.repository;

import com.schoolmgmt.model.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamTypeRepository extends JpaRepository<ExamType, UUID> {

    List<ExamType> findByTenantIdOrderByDisplayOrder(String tenantId);

    List<ExamType> findByTenantIdAndIsActiveTrue(String tenantId);

    boolean existsByTenantIdAndName(String tenantId, String name);
}
