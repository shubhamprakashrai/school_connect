package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {
    private UUID id;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String entityType;
    private String entityId;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static FileResponse from(FileEntity entity) {
        return FileResponse.builder()
                .id(entity.getId())
                .originalName(entity.getOriginalName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .downloadUrl("/files/" + entity.getId() + "/download")
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
