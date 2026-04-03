package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustInvoiceRequest {
    @NotNull(message = "Adjustment amount is required")
    private BigDecimal adjustmentAmount;
    private String adjustmentNote;
}
