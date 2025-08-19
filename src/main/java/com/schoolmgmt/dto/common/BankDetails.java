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
@Schema(description = "Bank details")
public class BankDetails {
    
    @Schema(description = "Bank name", example = "State Bank")
    private String bankName;
    
    @Schema(description = "Account number", example = "1234567890")
    private String accountNumber;
    
    @Schema(description = "Bank branch", example = "Main Branch")
    private String branch;
    
    @Schema(description = "IFSC code", example = "SBIN0001234")
    private String ifscCode;
}
