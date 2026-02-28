package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.schoolmgmt.dto.response.SectionResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a school class")
public class SchoolClassResponse {

    @Schema(description = "Unique ID of the class")
    private UUID id;

    @Schema(description = "Class code/identifier", example = "10")
    private String code;

    @Schema(description = "Readable class name", example = "Class 10")
    private String name;

    @Schema(description = "Description of the class")
    private String description;

    @Schema(description = "List of all sections inside this class")
    private List<SectionResponse> sections;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last updated timestamp")
    private LocalDateTime updatedAt;
}