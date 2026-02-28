package com.schoolmgmt.dto.request;

import lombok.Data;

@Data
public class ParentRequest {
    private String firstname;
    private String lastname;
    private String midlename;
    private String email;
    private String phone;
    private String parentType;
}
