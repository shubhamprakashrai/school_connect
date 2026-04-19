package com.schoolmgmt.dto.request;

import com.schoolmgmt.dto.common.UserRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import com.schoolmgmt.model.Teacher;
import com.schoolmgmt.model.User;

@Data
public class TeacherCreationRequest {

    @NotNull(message = "user details are required")
    @Schema(description = "user details for the school")
    private UserRequest userRequest;

    @Schema(description = "Additional roles for this teacher (e.g., ADMIN for teacher-admin)", 
            example = "[ADMIN]")
    private Set<User.UserRole> additionalRoles = new HashSet<>();

//    @NotBlank(message = "First name is required")
//    private String firstName;
//
//    @NotBlank(message = "Last name is required")
//    private String lastName;
//
//    @NotBlank(message = "Email is required")
//    @Email(message = "Invalid email format")
//    private String email;
//
//    @NotBlank(message = "Phone number is required")
//    private String phone;

//    @NotBlank(message = "Employee ID is required")
//    private String employeeId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Teacher.Gender gender;



    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Designation is required")
    private String designation;
}