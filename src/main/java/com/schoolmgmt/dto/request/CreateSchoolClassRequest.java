package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create school class request")
public class CreateSchoolClassRequest {


//    @Size(max = 20)
//    @NotBlank
//    @Schema(description = "Unique class code", example = "10")
//    private String classIdentifier;


    @Size(max = 100)
    @Schema(description = "Class name", example = "Class 10")
    private String name;

    @Size(max = 500)
    @Schema(description = "Class description")
    private String description;


}