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
    

    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId AND s.id = :id")
    Optional<Subject> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId AND s.name = :name")
    Optional<Subject> findByTenantIdAndName(@Param("tenantId") String tenantId, @Param("name") String name);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Subject> searchSubjects(@Param("searchTerm") String searchTerm, 
                                 @Param("tenantId") String tenantId, 
                                 Pageable pageable);
    
    @Query("SELECT s FROM Subject s WHERE s.tenantId = :tenantId")
    Page<Subject> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
    
    boolean existsByTenantIdAndCode(String tenantId, String code);
    
    boolean existsByTenantIdAndName(String tenantId, String name);
    
    @Query("SELECT s FROM Subject s JOIN s.classes c WHERE c.id = :classId")
    List<Subject> findByClassId(@Param("classId") UUID classId);
    
    @Query("SELECT DISTINCT s FROM Subject s JOIN s.teacherSubjects ts WHERE ts.teacher.id = :teacherId")
    List<Subject> findByTeacherId(@Param("teacherId") UUID teacherId);

}