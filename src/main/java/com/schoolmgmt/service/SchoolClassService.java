package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateSchoolClassRequest;
import com.schoolmgmt.dto.request.UpdateSchoolClassRequest;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SchoolClassService {

    SchoolClassResponse createClass(CreateSchoolClassRequest request);

    List<SchoolClassResponse> createClassesBulk(List<CreateSchoolClassRequest> requests);

    Page<SchoolClassResponse> getAllClasses(Pageable pageable);

    SchoolClassResponse getClassById(UUID classId);

    SchoolClassResponse updateClass(UUID classId, UpdateSchoolClassRequest request);

    void deleteClass(UUID classId);
}