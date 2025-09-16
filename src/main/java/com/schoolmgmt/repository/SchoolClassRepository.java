package com.schoolmgmt.repository;

import com.schoolmgmt.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    
    @Query("SELECT sc FROM SchoolClass sc WHERE sc.tenantId = :tenantId")
    List<SchoolClass> findAllByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT sc FROM SchoolClass sc WHERE sc.tenantId = :tenantId AND sc.code = :code")
    Optional<SchoolClass> findByTenantIdAndCode(@Param("tenantId") String tenantId, @Param("code") String code);
    
    @Query("SELECT sc FROM SchoolClass sc WHERE sc.id = :id AND sc.tenantId = :tenantId")
    Optional<SchoolClass> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);
}