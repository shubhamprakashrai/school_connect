package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.CustomRole;
import lombok.Builder;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class RoleResponse {
    UUID id;
    String name;
    String description;
    boolean isSystem;
    Set<String> permissions;

    public static RoleResponse from(CustomRole role) {
        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .isSystem(Boolean.TRUE.equals(role.getIsSystem()))
            .permissions(role.getPermissions())
            .build();
    }
}
