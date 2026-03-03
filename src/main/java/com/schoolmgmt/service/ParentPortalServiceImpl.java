package com.schoolmgmt.service;

import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.exception.AccessDeniedException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.mapper.StudentMapper;
import com.schoolmgmt.model.Parent;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.repository.ParentRepository;
import com.schoolmgmt.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Parent Portal operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParentPortalServiceImpl implements ParentPortalService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final com.schoolmgmt.util.TenantTokenUtil tenantTokenUtil;

    @Override
    public List<StudentResponse> getMyStudents() {
        Parent parent = getCurrentParent();
        String tenantId = com.schoolmgmt.util.TenantContext.getCurrentTenant();
        String userRole = tenantTokenUtil.extractRoleFromCurrentToken();
        
        log.info("Fetching students for parent: {} ({}) in tenant: {}", parent.getFullName(), parent.getId(), tenantId);
        
        List<Student> allStudents;
        
        // For admin users, get all students in tenant
        if ("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            log.info("Admin user {} accessing all students in tenant: {}", parent.getId(), tenantId);
            Pageable pageable = PageRequest.of(0, 1000); // Get up to 1000 students
            Page<Student> studentPage = studentRepository.findByTenantId(tenantId, pageable);
            allStudents = studentPage.getContent();
        } else {
            // For parent users, get only their associated students
            allStudents = parent.getAllStudents().stream()
                    .filter(student -> tenantId.equals(student.getTenantId()))
                    .collect(Collectors.toList());
        }
        
        List<StudentResponse> responses = allStudents.stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
        
        log.info("Found {} students for parent {} in tenant {}", responses.size(), parent.getId(), tenantId);
        
        return responses;
    }

    @Override
    public StudentResponse getStudentDetails(UUID studentId) {
        Parent parent = getCurrentParent();
        String tenantId = com.schoolmgmt.util.TenantContext.getCurrentTenant();
        String userRole = tenantTokenUtil.extractRoleFromCurrentToken();
        
        log.info("Parent {} requesting details for student {} in tenant: {}", parent.getId(), studentId, tenantId);
        
        // For admin users, check if student exists in tenant (not just parent's students)
        if ("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            log.info("Admin user {} accessing student {} details in tenant: {}", parent.getId(), studentId, tenantId);
            // Find student directly from repository for admin access
            Student student = studentRepository.findByIdAndTenantId(studentId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            return studentMapper.toResponse(student);
        }
        
        // For parent users, check if parent has access to this student
        if (!hasAccessToStudent(studentId)) {
            log.warn("Parent {} attempted to access student {} without permission in tenant: {}", parent.getId(), studentId, tenantId);
            throw new AccessDeniedException("You don't have permission to access this student's details");
        }
        
        // Find the student with tenant verification
        Student student = parent.getAllStudents().stream()
                .filter(s -> s.getId().equals(studentId) && tenantId.equals(s.getTenantId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        log.info("Parent {} accessing student {} details in tenant: {}", parent.getId(), studentId, tenantId);
        
        return studentMapper.toResponse(student);
    }

    @Override
    public boolean hasAccessToStudent(UUID studentId) {
        Parent parent = getCurrentParent();
        String tenantId = com.schoolmgmt.util.TenantContext.getCurrentTenant();
        String userRole = tenantTokenUtil.extractRoleFromCurrentToken();
        
        // For admin users, always grant access (they can see any student in tenant)
        if ("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            log.info("Admin user {} granted access to student {} in tenant: {}", parent.getId(), studentId, tenantId);
            return true;
        }
        
        // For parent users, check if student belongs to this parent
        boolean hasAccess = parent.getAllStudents().stream()
                .anyMatch(s -> s.getId().equals(studentId) && tenantId.equals(s.getTenantId()));
        
        log.debug("Parent {} access to student {} in tenant {}: {}", parent.getId(), studentId, tenantId, hasAccess);
        
        return hasAccess;
    }

    @Override
    public Parent getCurrentParent() {
        String tenantId = com.schoolmgmt.util.TenantContext.getCurrentTenant();
        String username = tenantTokenUtil.extractUsernameFromCurrentToken();
        String userRole = tenantTokenUtil.extractRoleFromCurrentToken();
        
        if (username == null) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        
        log.debug("Finding parent for user: {} (role: {}) in tenant: {}", username, userRole, tenantId);
        
        Parent parent;
        
        // If user is ADMIN or SUPER_ADMIN, find any parent in the tenant for testing
        if ("ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            log.info("Admin user {} accessing parent portal, finding first available parent in tenant: {}", username, tenantId);
            parent = parentRepository.findAllByTenantId(tenantId).stream()
                    .filter(Parent::isActive)
                    .filter(Parent::getPortalAccessEnabled)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No active parent found in tenant"));
        } else {
            // For regular parent users, find by email
            parent = parentRepository.findByEmailAndTenantId(username, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent not found for current user"));
        }
        
        if (!parent.isActive()) {
            throw new AccessDeniedException("Parent account is not active");
        }
        
        if (!parent.getPortalAccessEnabled()) {
            throw new AccessDeniedException("Portal access is disabled for this account");
        }
        
        log.debug("Found parent: {} ({})", parent.getFullName(), parent.getId());
        
        return parent;
    }
}
