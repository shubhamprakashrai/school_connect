package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSchoolClassRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}