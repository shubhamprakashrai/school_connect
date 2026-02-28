package com.schoolmgmt.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ParentResponse {
    private UUID parentId;
    private String firstname;
    private String lastname;
    private String middlename;
    private String email;
    private String phone;
    private String userId;
    private List<UUID> studentIds;
}
