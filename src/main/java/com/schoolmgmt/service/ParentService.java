package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.ParentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.model.Parent;
import com.schoolmgmt.model.Student;

import java.util.List;
import java.util.UUID;

public interface ParentService {

    ParentResponse createParent(ParentRequest request);

    Parent getOrCreateParentForStudent(Student student, ParentRequest request);

    ParentResponse updateParent(UUID parentId, ParentRequest request);

    void deleteParent(UUID parentId);

    ParentResponse getParent(UUID parentId);

    List<ParentResponse> getAllParents();
}