package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssignSubscriptionRequest {
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private LocalDate startDate;
    private Boolean autoRenew = true;
}
