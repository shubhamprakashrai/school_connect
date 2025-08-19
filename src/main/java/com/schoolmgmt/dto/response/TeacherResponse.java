package com.schoolmgmt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Teacher response")
public class TeacherResponse {
    private String id;
    private String employeeId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private LocalDate joiningDate;
    private String employeeType;
    private String department;
    private String designation;
    private Set<String> subjects;
    private Set<String> classes;
    private Boolean isClassTeacher;
    private String classTeacherFor;
    private String highestQualification;
    private Integer experienceYears;
    private String status;
    private String photoUrl;
    private Double rating;
    private Integer age;
    private Integer serviceYears;
}
