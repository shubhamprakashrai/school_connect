package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkPaidRequest {
    @NotBlank(message = "Payment reference is required")
    private String paymentReference;
    private String note;
}
