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
@Schema(description = "Admin information")
public class AdminInfo {
    private String id;
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isTemporaryPassword;

}
