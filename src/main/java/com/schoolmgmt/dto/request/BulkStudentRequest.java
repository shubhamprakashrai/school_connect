package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.request.CreateStudentRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk student creation request")
public class BulkStudentRequest {
    
    @NotNull(message = "Students list cannot be null")
    @NotEmpty(message = "At least one student must be provided")
    @Valid
    private List<CreateStudentRequest> students;

    //front-end to provide the role and usenname of person who is uploading the bulk students.
    @Schema(description = "Optional reference for tracking this bulk operation", example = "BATCH-2024-001")
    private String batchReference;
    
    @Schema(description = "Continue processing even if some students fail", example = "true")
    @Builder.Default
    private boolean continueOnError = true;
}
