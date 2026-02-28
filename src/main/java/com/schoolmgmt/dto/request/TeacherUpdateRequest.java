package com.schoolmgmt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating teacher information.
 */
@Data
@Schema(description = "Teacher update request")
public class TeacherUpdateRequest {

    @Schema(description = "Teacher's first name")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Schema(description = "Teacher's last name")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Schema(description = "Teacher's email address")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "Teacher's phone number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @Schema(description = "Teacher's address")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Schema(description = "Teacher's designation/position")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    @Schema(description = "Teacher's department")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Schema(description = "Teacher's status (ACTIVE, INACTIVE, SUSPENDED)")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
}
