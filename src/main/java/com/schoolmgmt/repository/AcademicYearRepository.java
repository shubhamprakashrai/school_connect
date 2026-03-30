package com.schoolmgmt.repository;

import com.schoolmgmt.model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    @Query("SELECT a FROM AcademicYear a WHERE a.tenantId = :tenantId AND a.isActive = true AND a.isDeleted = false")
    Optional<AcademicYear> findActiveByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT a FROM AcademicYear a WHERE a.tenantId = :tenantId AND a.name = :name AND a.isDeleted = false")
    Optional<AcademicYear> findByTenantIdAndName(@Param("tenantId") String tenantId, @Param("name") String name);

    List<AcademicYear> findByTenantIdAndIsDeletedFalse(String tenantId);

    Optional<AcademicYear> findByIdAndTenantIdAndIsDeletedFalse(UUID id, String tenantId);
}