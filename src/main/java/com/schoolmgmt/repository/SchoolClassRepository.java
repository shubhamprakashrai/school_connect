package com.schoolmgmt.repository;

import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
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
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {


    boolean existsByTenantIdAndNameIgnoreCase(String tenantId, String name);

    @Query("""
           SELECT MAX(CAST(SUBSTRING(c.code, LENGTH(:tenantId) + 3) AS integer))
           FROM SchoolClass c 
           WHERE c.tenantId = :tenantId
           """)
    Integer findMaxSequenceForTenant(@Param("tenantId") String tenantId);

    Page<SchoolClass> findByTenantId(String tenantId, Pageable pageable);
    
    List<SchoolClass> findAllByTenantId(String tenantId);


    // In SchoolClassRepository
    Optional<SchoolClass> findByIdAndTenantId(UUID id, String tenantId);

}