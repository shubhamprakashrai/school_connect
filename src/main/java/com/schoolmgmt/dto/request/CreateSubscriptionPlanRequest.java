package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.SubscriptionPlan.BillingCycle;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSubscriptionPlanRequest {
    @NotBlank(message = "Plan name is required")
    @Size(max = 100)
    private String name;

    private String tenantId;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.00")
    private BigDecimal basePrice;

    @NotNull(message = "Per-student price is required")
    @DecimalMin(value = "0.00")
    private BigDecimal perStudentPrice;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @Min(1) @Max(90)
    private Integer gracePeriodDays = 7;

    @Min(1) @Max(90)
    private Integer readOnlyPeriodDays = 7;

    @Min(1) @Max(30)
    private Integer minDaysForBilling = 10;
}
