package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant filter request for SuperAdmin")
public class TenantFilterRequest {

    @Schema(description = "Tenant name search term", example = "Green Valley School")
    private String name;

    @Schema(description = "Subdomain search term", example = "greenvalley")
    private String subdomain;

    @Schema(description = "Email search term", example = "admin@greenvalley.edu")
    private String email;

    @Schema(description = "Tenant status filter", example = "ACTIVE")
    private String status;

    @Schema(description = "Subscription plan filter", example = "PREMIUM")
    private String subscriptionPlan;

    @Schema(description = "City filter", example = "New York")
    private String city;

    @Schema(description = "State filter", example = "NY")
    private String state;

    @Schema(description = "Country filter", example = "USA")
    private String country;

    @Schema(description = "Created after date", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAfter;

    @Schema(description = "Created before date", example = "2024-12-31T23:59:59")
    private LocalDateTime createdBefore;

    @Schema(description = "General search term (searches across name, subdomain, email)", example = "green")
    private String search;

    @Schema(description = "Sort field", example = "createdAt")
    private String sortField;

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection;
}