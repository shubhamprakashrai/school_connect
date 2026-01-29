package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create parent/guardian request")
public class CreateParentRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(description = "First name", example = "Rajesh")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "Middle name", example = "Kumar")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Schema(description = "Last name", example = "Sharma")
    private String lastName;

    @NotBlank(message = "Parent type is required")
    @Pattern(regexp = "^(FATHER|MOTHER|GUARDIAN|GRANDFATHER|GRANDMOTHER|UNCLE|AUNT|BROTHER|SISTER|OTHER)$")
    @Schema(description = "Parent type", example = "FATHER")
    private String parentType;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$")
    @Schema(description = "Gender", example = "MALE")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1980-05-15")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    @Schema(description = "Email address", example = "rajesh.sharma@example.com")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Phone number", example = "+919876543210")
    private String phone;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Alternate phone number")
    private String alternatePhone;

    @Schema(description = "Work phone number")
    private String workPhone;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "City", example = "Mumbai")
    private String city;

    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;

    @Schema(description = "Postal code", example = "400001")
    private String postalCode;

    @Schema(description = "Occupation", example = "Software Engineer")
    private String occupation;

    @Schema(description = "Employer name")
    private String employer;

    @Schema(description = "Work address")
    private String workAddress;

    @Schema(description = "Annual income")
    private String annualIncome;

    @Schema(description = "Education level", example = "Post Graduate")
    private String educationLevel;

    @Schema(description = "Aadhar number")
    private String aadharNumber;

    @Schema(description = "Relationship to student", example = "Father")
    private String relationshipToStudent;

    @Schema(description = "Is primary contact", example = "true")
    @Builder.Default
    private Boolean isPrimaryContact = false;

    @Schema(description = "Is emergency contact", example = "true")
    @Builder.Default
    private Boolean isEmergencyContact = false;

    @Schema(description = "Can pickup child", example = "true")
    @Builder.Default
    private Boolean canPickupChild = true;

    @Schema(description = "Preferred language", example = "English")
    @Builder.Default
    private String preferredLanguage = "English";

    @Schema(description = "Receive SMS notifications", example = "true")
    @Builder.Default
    private Boolean receiveSms = true;

    @Schema(description = "Receive email notifications", example = "true")
    @Builder.Default
    private Boolean receiveEmail = true;

    @Schema(description = "Receive app notifications", example = "true")
    @Builder.Default
    private Boolean receiveAppNotifications = true;

    @Schema(description = "Enable portal access", example = "true")
    @Builder.Default
    private Boolean portalAccessEnabled = true;

    @Schema(description = "Photo URL")
    private String photoUrl;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Special instructions")
    private String specialInstructions;

    @Schema(description = "Create user account for portal login", example = "false")
    private boolean createUserAccount = false;
}
