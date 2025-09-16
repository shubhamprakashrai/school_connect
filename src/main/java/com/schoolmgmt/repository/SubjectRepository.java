package com.schoolmgmt.repository;

import com.schoolmgmt.model.Subject;
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
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId")
    List<Subject> findAllByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId")
    Page<Subject> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId AND s.code = :code")
    Optional<Subject> findByTenantIdAndCode(@Param("tenantId") String tenantId, @Param("code") String code);
    
    @Query("SELECT s FROM Subject s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<Subject> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);
    
    @Query("SELECT s FROM Subject s WHERE s.code = :code AND s.tenantId = :tenantId")
    Optional<Subject> findByCodeAndTenantId(@Param("code") String code, @Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(s) > 0 FROM Subject s WHERE s.code = :code AND s.tenantId = :tenantId")
    boolean existsByCodeAndTenantId(@Param("code") String code, @Param("tenantId") String tenantId);
    
    @Query("SELECT s FROM Subject s JOIN s.classes c WHERE c.id = :classId AND s.tenantId = :tenantId")
    List<Subject> findByClassId(@Param("classId") UUID classId);
    
    @Query("SELECT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacherId = :teacherId AND s.tenantId = :tenantId")
    List<Subject> findByTeacherId(@Param("teacherId") UUID teacherId);
    
    @Query("SELECT s FROM Subject s WHERE (LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND s.tenantId = :tenantId")
    Page<Subject> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndTenantId(
            @Param("name") String name, 
            @Param("code") String code, 
            @Param("tenantId") String tenantId, 
            Pageable pageable);
}