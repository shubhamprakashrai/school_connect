package com.schoolmgmt.repository;

import com.schoolmgmt.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {

    List<FeeStructure> findByTenantIdAndClassId(String tenantId, UUID classId);

    List<FeeStructure> findByTenantIdAndClassIdAndIsActiveTrue(String tenantId, UUID classId);

    List<FeeStructure> findByTenantIdAndIsActiveTrue(String tenantId);

    List<FeeStructure> findByTenantIdAndAcademicYear(String tenantId, String academicYear);
}
