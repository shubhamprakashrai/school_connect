package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parent filter request for paginated listing")
public class ParentFilterRequest {

    @Schema(description = "Search term (name, email, phone)")
    private String search;

    @Schema(description = "Filter by parent type (FATHER, MOTHER, GUARDIAN, etc.)")
    private String parentType;

    @Schema(description = "Filter by status (ACTIVE, INACTIVE, BLOCKED, DELETED)")
    private String status;
}
