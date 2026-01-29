package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parent/guardian response")
public class ParentResponse {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String parentType;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private String alternatePhone;
    private String workPhone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String occupation;
    private String employer;
    private String workAddress;
    private String annualIncome;
    private String educationLevel;
    private String relationshipToStudent;
    private Boolean isPrimaryContact;
    private Boolean isEmergencyContact;
    private Boolean canPickupChild;
    private String preferredLanguage;
    private Boolean receiveSms;
    private Boolean receiveEmail;
    private Boolean receiveAppNotifications;
    private String status;
    private Boolean portalAccessEnabled;
    private String photoUrl;
    private String notes;
    private String specialInstructions;
    private List<LinkedStudentInfo> linkedStudents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Brief student info linked to parent")
    public static class LinkedStudentInfo {
        private String id;
        private String fullName;
        private String rollNumber;
        private String currentClassId;
        private String relationship; // "child" or "ward"
    }
}
