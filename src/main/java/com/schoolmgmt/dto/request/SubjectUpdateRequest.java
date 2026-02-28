package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating an existing subject")
public class SubjectUpdateRequest {

    @Size(max = 20, message = "Subject code must be at most 20 characters")
    @Schema(description = "Subject code", example = "MATH101")
    private String code;

    @Size(max = 100, message = "Subject name must be at most 100 characters")
    @Schema(description = "Subject name", example = "Mathematics")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(description = "Subject description", example = "Mathematics for Grade 10")
    private String description;
}
