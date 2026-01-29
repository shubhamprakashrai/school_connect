package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update parent/guardian request")
public class UpdateParentRequest {

    @Size(max = 100)
    @Schema(description = "First name")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "Middle name")
    private String middleName;

    @Size(max = 100)
    @Schema(description = "Last name")
    private String lastName;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Alternate phone number")
    private String alternatePhone;

    @Schema(description = "Work phone number")
    private String workPhone;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Country")
    private String country;

    @Schema(description = "Postal code")
    private String postalCode;

    @Schema(description = "Occupation")
    private String occupation;

    @Schema(description = "Employer name")
    private String employer;

    @Schema(description = "Work address")
    private String workAddress;

    @Schema(description = "Annual income")
    private String annualIncome;

    @Schema(description = "Education level")
    private String educationLevel;

    @Schema(description = "Relationship to student")
    private String relationshipToStudent;

    @Schema(description = "Is primary contact")
    private Boolean isPrimaryContact;

    @Schema(description = "Is emergency contact")
    private Boolean isEmergencyContact;

    @Schema(description = "Can pickup child")
    private Boolean canPickupChild;

    @Schema(description = "Preferred language")
    private String preferredLanguage;

    @Schema(description = "Receive SMS notifications")
    private Boolean receiveSms;

    @Schema(description = "Receive email notifications")
    private Boolean receiveEmail;

    @Schema(description = "Receive app notifications")
    private Boolean receiveAppNotifications;

    @Schema(description = "Enable portal access")
    private Boolean portalAccessEnabled;

    @Schema(description = "Photo URL")
    private String photoUrl;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Special instructions")
    private String specialInstructions;
}
