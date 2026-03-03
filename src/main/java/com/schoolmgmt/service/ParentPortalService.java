package com.schoolmgmt.service;

import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.model.Student;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Parent Portal operations
 */
public interface ParentPortalService {

    /**
     * Get all students associated with the current parent (both children and wards)
     * @return List of student responses
     */
    List<StudentResponse> getMyStudents();

    /**
     * Get specific student details if the current parent has access
     * @param studentId Student ID
     * @return Student response
     * @throws com.schoolmgmt.exception.AccessDeniedException if parent doesn't have access
     * @throws com.schoolmgmt.exception.ResourceNotFoundException if student not found
     */
    StudentResponse getStudentDetails(UUID studentId);

    /**
     * Check if current parent has access to a specific student
     * @param studentId Student ID
     * @return true if parent has access, false otherwise
     */
    boolean hasAccessToStudent(UUID studentId);

    /**
     * Get current parent entity from authenticated user
     * @return Parent entity
     * @throws com.schoolmgmt.exception.ResourceNotFoundException if parent not found
     */
    com.schoolmgmt.model.Parent getCurrentParent();
}
