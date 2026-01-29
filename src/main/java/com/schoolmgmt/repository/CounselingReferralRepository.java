package com.schoolmgmt.repository;

import com.schoolmgmt.model.CounselingReferral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CounselingReferralRepository extends JpaRepository<CounselingReferral, UUID> {

    Page<CounselingReferral> findByTenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);

    List<CounselingReferral> findByTenantIdAndStatusAndIsDeletedFalse(String tenantId, CounselingReferral.ReferralStatus status);

    List<CounselingReferral> findByTenantIdAndStudentIdAndIsDeletedFalse(String tenantId, String studentId);

    List<CounselingReferral> findByTenantIdAndUrgencyAndIsDeletedFalse(String tenantId, CounselingReferral.Urgency urgency);

    long countByTenantIdAndStatusAndIsDeletedFalse(String tenantId, CounselingReferral.ReferralStatus status);
}
