package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterDataResponse {

    private UUID id;
    private String category;
    private String value;
    private String label;
    private String description;
    private int displayOrder;
    private boolean isActive;
    private boolean isDefault;
}
