package com.schoolmgmt.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Schema(description = "Admin user details for tenant registration")
//public class AdminUserRequest {
//
//    @NotBlank(message = "Userid is required")
//    @Schema(description = "Admin userid ", example = "TE000100001")
//    private String userId;
//
//
//    @NotBlank(message = "Username is required")
//    @Size(min = 3, max = 50)
//    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
//    @Schema(description = "Admin username", example = "admin")
//    private String username;
//
//    @NotBlank(message = "Email is required")
//    @Email(message = "Invalid email format")
//    @Schema(description = "Admin email", example = "admin@greenwood.edu")
//    private String email;
//
//    @NotBlank(message = "Password is required")
//    @Size(min = 8)
//    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
//            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
//    @Schema(description = "Admin password", example = "Admin@123")
//    private String password;
//
//    @NotBlank(message = "First name is required")
//    @Size(max = 100)
//    @Schema(description = "Admin first name", example = "John")
//    private String firstName;
//
//    @NotBlank(message = "Last name is required")
//    @Size(max = 100)
//    @Schema(description = "Admin last name", example = "Doe")
//    private String lastName;
//
//    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
//    @Schema(description = "Admin phone number", example = "+1234567890")
//    private String phone;
//



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic user details for tenant or school")
public class UserRequest {

    @NotBlank
    @Schema(description = "First name of the user", example = "John")
    private String firstName;



    @Schema(description = "Last name of the user", example = "Doe")
    private String middleName;


    @NotBlank
    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;


    @NotBlank
    @Email
    @Schema(description = "Email of the user", example = "john.doe@school.edu")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

//    @NotBlank
//    @Schema(description = "User role", example = "TEACHER")
//    private String role;

    @Schema(description = "Profile image URL", example = "https://cdn.school.edu/avatar.png")
    private String avatarUrl;

    @Schema(description = "Reference ID for related entity, e.g., classId for student", example = "CLS001")
    private String referenceId;

    @Schema(description = "Reference type, e.g., CLASS, DEPARTMENT, STUDENT", example = "CLASS")
    private String referenceType;

    @Schema(description = "Tenant Identifier (optional if using context)", example = "TNT001")
    private String tenantId;
}

