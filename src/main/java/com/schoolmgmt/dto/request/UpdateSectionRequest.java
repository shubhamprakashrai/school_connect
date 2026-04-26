package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSectionRequest {
    @NotBlank
    @Size(max = 50)
    private String name;

    private Integer capacity;
}
