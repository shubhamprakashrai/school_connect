package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentResponse {
    private UUID parentId;
    private String firstname;
    private String middlename;
    private String lastname;
    private String email;
    private String phone;
    private String parentType;
    private String status;
    private Boolean portalAccessEnabled;
    private Boolean isPrimaryContact;
    private Boolean isEmergencyContact;
    private Boolean canPickupChild;
    private String preferredLanguage;
    private Boolean receiveSms;
    private Boolean receiveEmail;
    private Boolean receiveAppNotifications;
    private List<UUID> studentIds;
    private String userId;
}
