package com.schoolmgmt.controller;

import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.dto.response.FileResponse;
import com.schoolmgmt.model.FileEntity;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.FileRepository;
import com.schoolmgmt.service.FileStorageService;
import com.schoolmgmt.service.LocalFileStorageService;
import com.schoolmgmt.util.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "Upload, download, and manage files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;

    @Value("${file.allowed-extensions:jpg,jpeg,png,pdf,doc,docx,xls,xlsx,csv}")
    private String allowedExtensions;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) String entityId,
            @AuthenticationPrincipal User user) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        String extension = getExtension(file.getOriginalFilename());
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        if (!extension.isEmpty() && !allowed.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File type not allowed. Allowed: " + allowedExtensions));
        }

        String tenantId = TenantContext.getCurrentTenant();
        String storagePath = fileStorageService.store(file, tenantId);

        FileEntity fileEntity = FileEntity.builder()
                .tenantId(tenantId)
                .originalName(file.getOriginalFilename())
                .storedName(storagePath.substring(storagePath.lastIndexOf("/") + 1))
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .entityType(entityType)
                .entityId(entityId)
                .uploadedBy(user.getId())
                .build();

        FileEntity saved = fileRepository.save(fileEntity);
        log.info("File uploaded: {} by user: {} for tenant: {}", saved.getId(), user.getId(), tenantId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", FileResponse.from(saved)));
    }

    @PostMapping("/upload/multiple")
    @Operation(summary = "Upload multiple files")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) String entityId,
            @AuthenticationPrincipal User user) {

        String tenantId = TenantContext.getCurrentTenant();
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));

        List<FileResponse> responses = Arrays.stream(files)
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    String extension = getExtension(file.getOriginalFilename());
                    if (!extension.isEmpty() && !allowed.contains(extension.toLowerCase())) {
                        throw new IllegalArgumentException("File type not allowed: " + file.getOriginalFilename());
                    }

                    String storagePath = fileStorageService.store(file, tenantId);
                    FileEntity entity = FileEntity.builder()
                            .tenantId(tenantId)
                            .originalName(file.getOriginalFilename())
                            .storedName(storagePath.substring(storagePath.lastIndexOf("/") + 1))
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .storagePath(storagePath)
                            .entityType(entityType)
                            .entityId(entityId)
                            .uploadedBy(user.getId())
                            .build();
                    return FileResponse.from(fileRepository.save(entity));
                })
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Files uploaded successfully", responses));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a file (optional ?variant=thumb for image thumbnail)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable UUID id,
            @RequestParam(required = false) String variant) {
        String tenantId = TenantContext.getCurrentTenant();
        FileEntity fileEntity = fileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Resource resource;
        if (variant != null && fileStorageService instanceof LocalFileStorageService local) {
            resource = local.loadVariant(fileEntity.getStoragePath(), variant);
        } else {
            resource = fileStorageService.load(fileEntity.getStoragePath());
        }

        // Thumbnails are inline-viewed; keep original as attachment.
        String disposition = "thumb".equalsIgnoreCase(variant) ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        fileEntity.getContentType() != null ? fileEntity.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + fileEntity.getOriginalName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=604800")
                .body(resource);
    }

    @GetMapping
    @Operation(summary = "List files for current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getCurrentTenant();
        Page<FileEntity> files = fileRepository.findByTenantId(tenantId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<FileResponse> responses = files.map(FileResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Files retrieved", responses));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get files linked to a specific entity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getEntityFiles(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<FileEntity> files = fileRepository.findByTenantIdAndEntityTypeAndEntityId(
                tenantId, entityType, entityId);

        List<FileResponse> responses = files.stream().map(FileResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success("Entity files retrieved", responses));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        FileEntity fileEntity = fileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        fileStorageService.delete(fileEntity.getStoragePath());
        fileRepository.delete(fileEntity);

        log.info("File deleted: {} for tenant: {}", id, tenantId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
