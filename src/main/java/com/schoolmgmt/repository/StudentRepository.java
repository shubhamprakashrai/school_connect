package com.schoolmgmt.repository;

import com.schoolmgmt.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByRollNumberAndSchoolClassIdAndTenantId(String rollNumber, UUID classId, String tenantId);

    Page<Student> findByTenantId(String tenantId, Pageable pageable);

    List<Student> findBySchoolClassIdAndTenantId(UUID classId, String tenantId);

    List<Student> findBySchoolClassIdAndSectionIdAndTenantId(UUID classId, UUID sectionId, String tenantId);

    List<Student> findBySectionIdAndTenantId(UUID sectionId, String tenantId);

    Optional<Student> findByIdAndTenantId(UUID id, String tenantId);

    List<Student> findByStatusAndTenantId(Student.StudentStatus status, String tenantId);

    @Query("SELECT s FROM Student s JOIN s.parents p WHERE p.id = :parentId")
    List<Student> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT s FROM Student s JOIN s.guardians g WHERE g.id = :guardianId")
    List<Student> findByGuardianId(@Param("guardianId") UUID guardianId);

    boolean existsByRollNumberAndSchoolClassIdAndTenantId(String rollNumber, UUID classId, String tenantId);

    @Query("SELECT s FROM Student s WHERE s.tenantId = :tenantId AND " +
            "(LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Student> searchStudents(@Param("searchTerm") String searchTerm,
                                 @Param("tenantId") String tenantId,
                                 Pageable pageable);

    List<Student> findByAdmissionDateBetweenAndTenantId(LocalDate startDate, LocalDate endDate, String tenantId);

    long countBySchoolClassIdAndTenantIdAndStatus(UUID classId, String tenantId, Student.StudentStatus status);

    long countByTenantIdAndStatus(String tenantId, Student.StudentStatus status);

    @Modifying
    @Query("UPDATE Student s SET s.status = :status WHERE s.id = :studentId")
    void updateStatus(@Param("studentId") UUID studentId, @Param("status") Student.StudentStatus status);

    @Query("SELECT s FROM Student s WHERE s.tenantId = :tenantId AND " +
            "EXTRACT(MONTH FROM s.dateOfBirth) = :month AND " +
            "EXTRACT(DAY FROM s.dateOfBirth) BETWEEN :startDay AND :endDay")
    List<Student> findStudentsWithBirthdayInRange(@Param("tenantId") String tenantId,
                                                  @Param("month") int month,
                                                  @Param("startDay") int startDay,
                                                  @Param("endDay") int endDay);

    @Query("SELECT s.schoolClass.id, COUNT(s), " +
            "SUM(CASE WHEN s.gender = 'MALE' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.gender = 'FEMALE' THEN 1 ELSE 0 END) " +
            "FROM Student s WHERE s.tenantId = :tenantId AND s.status = 'ACTIVE' " +
            "GROUP BY s.schoolClass.id")
    List<Object[]> getStudentStatisticsByClass(@Param("tenantId") String tenantId);

    @Query("SELECT MAX(CAST(SUBSTRING(s.rollNumber, 3, 5) AS integer)) FROM Student s WHERE s.tenantId = :tenantId")
    Integer findMaxSequenceForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT MAX(CAST(SUBSTRING(s.rollNumber, 3, 5) AS integer)) FROM Student s WHERE s.schoolClass.id = :classId AND s.tenantId = :tenantId")
    Integer findMaxSequenceForClassAndTenant(@Param("classId") UUID classId, @Param("tenantId") String tenantId);

}