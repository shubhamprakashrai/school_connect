package com.schoolmgmt.dto.response;

import com.schoolmgmt.dto.common.TenantLimits;
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
@Schema(description = "Tenant response")
public class TenantResponse {
    private String id;
    private String identifier;
    private String name;
    private String subdomain;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String website;
    private String logoUrl;
    private String status;
    private String subscriptionPlan;
    private TenantLimits limits;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
}
