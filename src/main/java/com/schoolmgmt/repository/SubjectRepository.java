package com.schoolmgmt.repository;

import com.schoolmgmt.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId")
    List<Subject> findAllByTenantId(@Param("tenantId") UUID tenantId);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId AND s.code = :code")
    Optional<Subject> findByTenantIdAndCode(@Param("tenantId") UUID tenantId, @Param("code") String code);
}