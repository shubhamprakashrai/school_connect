package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject response")
public class SubjectResponse {

    @Schema(description = "Subject ID")
    private UUID id;

    @Schema(description = "Subject code")
    private String code;

    @Schema(description = "Subject name")
    private String name;

    @Schema(description = "Subject description")
    private String description;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
