package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentSubmissionRequest {
    @NotNull private UUID studentId;
    @NotNull private UUID channelId;
    private UUID feeId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank @Size(max = 500)
    private String screenshotUrl;

    @Size(max = 500)
    private String remarks;
}
