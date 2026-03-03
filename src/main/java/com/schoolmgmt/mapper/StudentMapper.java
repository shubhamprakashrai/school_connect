package com.schoolmgmt.mapper;

import com.schoolmgmt.dto.common.EmergencyContact;
import com.schoolmgmt.dto.common.ParentInfo;
import com.schoolmgmt.dto.response.SchoolClassResponse;
import com.schoolmgmt.dto.response.SectionResponse;
import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.model.Student;
import org.springframework.stereotype.Component;

/**
 * Mapper for Student entity and DTO
 */
@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        // Build parent info objects
        ParentInfo fatherInfo = null;
        if (student.getFatherName() != null) {
            fatherInfo = ParentInfo.builder()
                    .name(student.getFatherName())
                    .phone(student.getFatherPhone())
                    .email(student.getFatherEmail())
                    .occupation(student.getFatherOccupation())
                    .build();
        }

        ParentInfo motherInfo = null;
        if (student.getMotherName() != null) {
            motherInfo = ParentInfo.builder()
                    .name(student.getMotherName())
                    .phone(student.getMotherPhone())
                    .email(student.getMotherEmail())
                    .occupation(student.getMotherOccupation())
                    .build();
        }

        ParentInfo guardianInfo = null;
        if (student.getGuardianName() != null) {
            guardianInfo = ParentInfo.builder()
                    .name(student.getGuardianName())
                    .phone(student.getGuardianPhone())
                    .email(student.getGuardianEmail())
                    .parentType(student.getGuardianRelation())
                    .build();
        }

        EmergencyContact emergencyContact = null;
        if (student.getEmergencyContactName() != null) {
            emergencyContact = EmergencyContact.builder()
                    .name(student.getEmergencyContactName())
                    .relation(student.getEmergencyContactRelation())
                    .phone(student.getEmergencyContactPhone())
                    .build();
        }

        // Build school class response
        SchoolClassResponse schoolClassResponse = null;
        if (student.getSchoolClass() != null) {
            schoolClassResponse = SchoolClassResponse.builder()
                    .id(student.getSchoolClass().getId())
                    .name(student.getSchoolClass().getName())
                    .code(student.getSchoolClass().getCode())
                    .build();
        }

        // Build section response
        SectionResponse sectionResponse = null;
        if (student.getSection() != null) {
            sectionResponse = SectionResponse.builder()
                    .id(student.getSection().getId())
                    .name(student.getSection().getName())
                    .capacity(student.getSection().getCapacity())
                    .build();
        }

        return StudentResponse.builder()
                .id(student.getId() != null ? student.getId().toString() : null)
                .rollNumber(student.getRollNumber())
                .firstName(student.getFirstName())
                .middleName(student.getMiddleName())
                .lastName(student.getLastName())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender() != null ? student.getGender().toString() : null)
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .city(student.getCity())
                .state(student.getState())
                .country(student.getCountry())
                .postalCode(student.getPostalCode())
                .schoolClass(schoolClassResponse)
                .section(sectionResponse)
                .admissionDate(student.getAdmissionDate())
                .status(student.getStatus() != null ? student.getStatus().toString() : null)
                .photoUrl(student.getPhotoUrl())
                .fatherInfo(fatherInfo)
                .motherInfo(motherInfo)
                .guardianInfo(guardianInfo)
                .emergencyContact(emergencyContact)
                .build();
    }
}
