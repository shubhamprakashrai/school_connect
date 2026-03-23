package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterDataRequest {

    @NotNull(message = "Category is required")
    private String category;

    @NotBlank(message = "Value is required")
    private String value;

    @NotBlank(message = "Label is required")
    private String label;

    private String description;

    @Builder.Default
    private int displayOrder = 0;
}
