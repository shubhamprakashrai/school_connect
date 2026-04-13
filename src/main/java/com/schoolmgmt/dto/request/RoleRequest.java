package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {

    @NotBlank
    @Size(min = 2, max = 80)
    private String name;

    @Size(max = 280)
    private String description;

    /** Permission keys from {@link com.schoolmgmt.security.Permission}. */
    private Set<String> permissions;
}
