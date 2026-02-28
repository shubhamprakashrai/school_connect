package com.schoolmgmt.repository;

import com.schoolmgmt.model.Section;
import com.schoolmgmt.model.SchoolClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {

    // Using the association's id
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.schoolClass.id = :schoolClassId")
    List<Section> findByTenantIdAndSchoolClassId(@Param("tenantId") String tenantId,
                                                 @Param("schoolClassId") UUID schoolClassId);

    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.schoolClass.id = :schoolClassId AND s.name = :name")
    Optional<Section> findByTenantIdAndSchoolClassIdAndName(@Param("tenantId") String tenantId,
                                                            @Param("schoolClassId") UUID schoolClassId,
                                                            @Param("name") String name);

    // Optional alternative: pass the SchoolClass entity directly
    List<Section> findByTenantIdAndSchoolClass(String tenantId, SchoolClass schoolClass);


    @Query("SELECT s FROM Section s JOIN FETCH s.schoolClass WHERE s.id = :id")
    Optional<Section> findByIdWithClass(@Param("id") UUID id);

    boolean existsBySchoolClassAndName(SchoolClass schoolClass, String name);

    List<Section> findBySchoolClass(SchoolClass schoolClass);


//        List<Section> findBySchoolClass(SchoolClass schoolClass);
//
//        boolean existsBySchoolClassAndName(SchoolClass schoolClass, String name);

    // NEW: Find section(s) by section name
    List<Section> findByName(String name);

    // Optional: fetch section by class name (joins SchoolClass)
    List<Section> findBySchoolClass_Name(String className);


    // In SectionRepository
    Optional<Section> findByIdAndTenantId(UUID id, String tenantId);

    @Query("SELECT s FROM Section s WHERE s.classTeacher.id = :teacherId AND s.tenantId = :tenantId")
    List<Section> findByClassTeacherIdAndTenantId(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE Section s SET s.classTeacher = null WHERE s.classTeacher.id = :teacherId AND s.tenantId = :tenantId")
    int removeTeacherFromAllSections(@Param("teacherId") UUID teacherId, @Param("tenantId") String tenantId);

    @Query("SELECT s FROM Section s WHERE s.classTeacher IS NOT NULL AND s.tenantId = :tenantId")
    Page<Section> findByClassTeacherIsNotNullAndTenantId(@Param("tenantId") String tenantId, Pageable pageable);

}

