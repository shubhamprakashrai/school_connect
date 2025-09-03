package com.schoolmgmt.repository;

import com.schoolmgmt.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.schoolClassId = :schoolClassId")
    List<Section> findByTenantIdAndSchoolClassId(@Param("tenantId") UUID tenantId, @Param("schoolClassId") UUID schoolClassId);
    
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.schoolClassId = :schoolClassId AND s.name = :name")
    Optional<Section> findByTenantIdAndSchoolClassIdAndName(@Param("tenantId") UUID tenantId, @Param("schoolClassId") UUID schoolClassId, @Param("name") String name);
}