package com.schoolmgmt.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.schoolmgmt.dto.response.BulkImportResult;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.model.Student;
import com.schoolmgmt.repository.StudentRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service for bulk student import/export operations via CSV.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentBulkService {

    private final StudentRepository studentRepository;

    private static final String[] CSV_HEADERS = {
            "rollNumber", "firstName", "lastName", "dateOfBirth", "gender",
            "email", "phone", "address", "currentClassId", "currentSectionId",
            "guardianName", "guardianPhone", "guardianEmail",
            "admissionDate", "status"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Export students to CSV based on optional filters.
     */
    @Transactional(readOnly = true)
    public byte[] exportStudentsToCsv(String classId, String sectionId, String status) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Exporting students to CSV for tenant: {}, classId: {}, sectionId: {}, status: {}",
                tenantId, classId, sectionId, status);

        List<Student> students = findStudentsWithFilters(tenantId, classId, sectionId, status);

        try (StringWriter stringWriter = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(stringWriter)) {

            // Write headers
            csvWriter.writeNext(CSV_HEADERS);

            // Write student data
            for (Student student : students) {
                String[] row = {
                        nullSafe(student.getRollNumber()),
                        nullSafe(student.getFirstName()),
                        nullSafe(student.getLastName()),
                        student.getDateOfBirth() != null ? student.getDateOfBirth().format(DATE_FORMATTER) : "",
                        student.getGender() != null ? student.getGender().name() : "",
                        nullSafe(student.getEmail()),
                        nullSafe(student.getPhone()),
                        nullSafe(student.getAddress()),
                        nullSafe(student.getCurrentClassId()),
                        student.getCurrentSectionId() != null ? student.getCurrentSectionId().toString() : "",
                        nullSafe(student.getGuardianName()),
                        nullSafe(student.getGuardianPhone()),
                        nullSafe(student.getGuardianEmail()),
                        student.getAdmissionDate() != null ? student.getAdmissionDate().format(DATE_FORMATTER) : "",
                        student.getStatus() != null ? student.getStatus().name() : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            log.info("Exported {} students to CSV", students.size());
            return stringWriter.toString().getBytes();

        } catch (IOException e) {
            log.error("Failed to export students to CSV", e);
            throw new BusinessException("Failed to export students to CSV: " + e.getMessage());
        }
    }

    /**
     * Import students from a CSV file.
     */
    public BulkImportResult importStudentsFromCsv(MultipartFile file, String classId) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.info("Importing students from CSV for tenant: {}, classId: {}", tenantId, classId);

        if (file.isEmpty()) {
            throw new BusinessException("CSV file is empty");
        }

        List<BulkImportResult.ImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (InputStreamReader inputReader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(inputReader)) {

            // Read and validate headers
            String[] headers = csvReader.readNext();
            if (headers == null || headers.length == 0) {
                throw new BusinessException("CSV file has no headers");
            }
            validateHeaders(headers);

            // Build header-to-index map for flexible column ordering
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                totalRows++;
                int rowNumber = totalRows + 1; // +1 for header row

                try {
                    Student student = parseStudentRow(row, headerMap, classId, tenantId, rowNumber, errors);
                    if (student != null) {
                        studentRepository.save(student);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("Error importing row {}: {}", rowNumber, e.getMessage());
                    errors.add(BulkImportResult.ImportError.builder()
                            .rowNumber(rowNumber)
                            .fieldName("row")
                            .errorMessage("Unexpected error: " + e.getMessage())
                            .build());
                }
            }

        } catch (IOException | CsvValidationException e) {
            log.error("Failed to read CSV file", e);
            throw new BusinessException("Failed to read CSV file: " + e.getMessage());
        }

        log.info("Import completed: total={}, success={}, errors={}", totalRows, successCount, errors.size());

        return BulkImportResult.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .errorCount(errors.size())
                .errors(errors)
                .build();
    }

    /**
     * Generate an empty CSV template with headers only.
     */
    public byte[] generateImportTemplate() {
        log.info("Generating CSV import template");

        try (StringWriter stringWriter = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(stringWriter)) {

            csvWriter.writeNext(CSV_HEADERS);

            // Write one example row
            String[] exampleRow = {
                    "001", "John", "Doe", "2010-05-15", "MALE",
                    "john.doe@example.com", "1234567890", "123 Main St",
                    "", "", "Jane Doe", "0987654321", "jane.doe@example.com",
                    "2024-04-01", "ACTIVE"
            };
            csvWriter.writeNext(exampleRow);

            csvWriter.flush();
            return stringWriter.toString().getBytes();

        } catch (IOException e) {
            log.error("Failed to generate CSV template", e);
            throw new BusinessException("Failed to generate CSV template: " + e.getMessage());
        }
    }

    // ---- Private helper methods ----

    private List<Student> findStudentsWithFilters(String tenantId, String classId, String sectionId, String status) {
        if (classId != null && sectionId != null) {
            UUID sectionUuid = UUID.fromString(sectionId);
            return studentRepository.findByCurrentClassIdAndCurrentSectionIdAndTenantId(classId, sectionUuid, tenantId);
        } else if (classId != null) {
            return studentRepository.findByCurrentClassIdAndTenantId(classId, tenantId);
        } else if (status != null) {
            Student.StudentStatus studentStatus = Student.StudentStatus.valueOf(status.toUpperCase());
            return studentRepository.findByStatusAndTenantId(studentStatus, tenantId);
        } else {
            return studentRepository.findByTenantId(tenantId,
                    org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
    }

    private void validateHeaders(String[] headers) {
        Set<String> requiredHeaders = Set.of("rollnumber", "firstname", "lastname", "dateofbirth", "gender");
        Set<String> providedHeaders = new HashSet<>();
        for (String header : headers) {
            providedHeaders.add(header.trim().toLowerCase());
        }

        for (String required : requiredHeaders) {
            if (!providedHeaders.contains(required)) {
                throw new BusinessException("Missing required CSV header: " + required);
            }
        }
    }

    private Student parseStudentRow(String[] row, Map<String, Integer> headerMap, String classId,
                                     String tenantId, int rowNumber, List<BulkImportResult.ImportError> errors) {
        boolean hasError = false;

        // Required fields
        String rollNumber = getField(row, headerMap, "rollnumber");
        String firstName = getField(row, headerMap, "firstname");
        String lastName = getField(row, headerMap, "lastname");
        String dateOfBirthStr = getField(row, headerMap, "dateofbirth");
        String genderStr = getField(row, headerMap, "gender");

        // Validate required fields
        if (rollNumber == null || rollNumber.isBlank()) {
            errors.add(buildError(rowNumber, "rollNumber", "Roll number is required", rollNumber));
            hasError = true;
        }
        if (firstName == null || firstName.isBlank()) {
            errors.add(buildError(rowNumber, "firstName", "First name is required", firstName));
            hasError = true;
        }
        if (lastName == null || lastName.isBlank()) {
            errors.add(buildError(rowNumber, "lastName", "Last name is required", lastName));
            hasError = true;
        }

        // Parse date of birth
        LocalDate dateOfBirth = null;
        if (dateOfBirthStr == null || dateOfBirthStr.isBlank()) {
            errors.add(buildError(rowNumber, "dateOfBirth", "Date of birth is required", dateOfBirthStr));
            hasError = true;
        } else {
            try {
                dateOfBirth = LocalDate.parse(dateOfBirthStr.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(buildError(rowNumber, "dateOfBirth", "Invalid date format. Use yyyy-MM-dd", dateOfBirthStr));
                hasError = true;
            }
        }

        // Parse gender
        Student.Gender gender = null;
        if (genderStr == null || genderStr.isBlank()) {
            errors.add(buildError(rowNumber, "gender", "Gender is required (MALE, FEMALE, OTHER)", genderStr));
            hasError = true;
        } else {
            try {
                gender = Student.Gender.valueOf(genderStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(buildError(rowNumber, "gender", "Invalid gender. Use MALE, FEMALE, or OTHER", genderStr));
                hasError = true;
            }
        }

        if (hasError) {
            return null;
        }

        // Optional fields
        String email = getField(row, headerMap, "email");
        String phone = getField(row, headerMap, "phone");
        String address = getField(row, headerMap, "address");
        String csvClassId = getField(row, headerMap, "currentclassid");
        String sectionIdStr = getField(row, headerMap, "currentsectionid");
        String guardianName = getField(row, headerMap, "guardianname");
        String guardianPhone = getField(row, headerMap, "guardianphone");
        String guardianEmail = getField(row, headerMap, "guardianemail");
        String admissionDateStr = getField(row, headerMap, "admissiondate");
        String statusStr = getField(row, headerMap, "status");

        // Use classId from parameter if not specified in CSV
        String effectiveClassId = (csvClassId != null && !csvClassId.isBlank()) ? csvClassId : classId;

        // Parse section ID
        UUID sectionId = null;
        if (sectionIdStr != null && !sectionIdStr.isBlank()) {
            try {
                sectionId = UUID.fromString(sectionIdStr.trim());
            } catch (IllegalArgumentException e) {
                errors.add(buildError(rowNumber, "currentSectionId", "Invalid section ID format (UUID expected)", sectionIdStr));
                return null;
            }
        }

        // Parse admission date
        LocalDate admissionDate = LocalDate.now();
        if (admissionDateStr != null && !admissionDateStr.isBlank()) {
            try {
                admissionDate = LocalDate.parse(admissionDateStr.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(buildError(rowNumber, "admissionDate", "Invalid date format. Use yyyy-MM-dd", admissionDateStr));
                return null;
            }
        }

        // Parse status
        Student.StudentStatus studentStatus = Student.StudentStatus.ACTIVE;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                studentStatus = Student.StudentStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(buildError(rowNumber, "status", "Invalid status value", statusStr));
                return null;
            }
        }

        // Check for duplicate roll number in class
        if (effectiveClassId != null && rollNumber != null &&
                studentRepository.existsByRollNumberAndCurrentClassIdAndTenantId(rollNumber, effectiveClassId, tenantId)) {
            errors.add(buildError(rowNumber, "rollNumber", "Roll number already exists in this class", rollNumber));
            return null;
        }

        // Build student
        Student student = Student.builder()
                .rollNumber(rollNumber)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .email(email)
                .phone(phone)
                .address(address)
                .currentClassId(effectiveClassId)
                .currentSectionId(sectionId)
                .guardianName(guardianName)
                .guardianPhone(guardianPhone)
                .guardianEmail(guardianEmail)
                .admissionDate(admissionDate)
                .status(studentStatus)
                .emergencyContactPhone(guardianPhone != null ? guardianPhone : "N/A")
                .build();

        student.setTenantId(tenantId);
        return student;
    }

    private String getField(String[] row, Map<String, Integer> headerMap, String headerName) {
        Integer index = headerMap.get(headerName);
        if (index == null || index >= row.length) {
            return null;
        }
        String value = row[index].trim();
        return value.isEmpty() ? null : value;
    }

    private BulkImportResult.ImportError buildError(int rowNumber, String fieldName, String message, String rawValue) {
        return BulkImportResult.ImportError.builder()
                .rowNumber(rowNumber)
                .fieldName(fieldName)
                .errorMessage(message)
                .rawValue(rawValue)
                .build();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
