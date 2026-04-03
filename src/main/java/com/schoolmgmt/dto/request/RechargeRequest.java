package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RechargeRequest {
    @NotNull(message = "Number of months is required")
    @Min(1) @Max(12)
    private Integer months;
}
