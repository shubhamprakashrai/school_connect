package com.schoolmgmt.dto.response;

import com.schoolmgmt.dto.common.EmergencyContact;
import com.schoolmgmt.dto.common.ParentInfo;
import com.schoolmgmt.dto.common.TransportInfo;
import com.schoolmgmt.model.SchoolClass;
import com.schoolmgmt.model.Section;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student response")
public class StudentResponse {
    private String id;
    private String rollNumber;
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
    private SchoolClassResponse schoolClass;
    private SectionResponse section;
    private LocalDate admissionDate;
    private String status;
    private String photoUrl;
    private ParentInfo fatherInfo;
    private ParentInfo motherInfo;
    private ParentInfo guardianInfo;
    private EmergencyContact emergencyContact;
}
