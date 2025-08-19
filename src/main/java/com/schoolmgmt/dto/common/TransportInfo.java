package com.schoolmgmt.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transport information")
public class TransportInfo {
    
    @Schema(description = "Transport mode", example = "BUS")
    private String transportMode;
    
    @Schema(description = "Transport route ID")
    private String transportRouteId;
    
    @Schema(description = "Pickup point")
    private String pickupPoint;
}
