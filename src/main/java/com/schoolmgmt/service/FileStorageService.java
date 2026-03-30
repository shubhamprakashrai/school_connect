package com.schoolmgmt.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String tenantId);
    Resource load(String storagePath);
    void delete(String storagePath);
}
