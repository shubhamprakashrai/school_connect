package com.schoolmgmt.dto.common;

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
@Schema(description = "Tenant information")
public class TenantInfo {
    private String id;
    private String identifier;
    private String name;
    private String subdomain;
    private String status;
    private String subscriptionPlan;
    private LocalDateTime activatedAt;
}
