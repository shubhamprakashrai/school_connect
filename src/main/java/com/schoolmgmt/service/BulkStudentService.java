package com.schoolmgmt.service;

import com.schoolmgmt.dto.common.EmergencyContact;
import com.schoolmgmt.dto.common.ParentInfo;
import com.schoolmgmt.dto.request.BulkStudentRequest;
import com.schoolmgmt.dto.request.CreateStudentRequest;
import com.schoolmgmt.dto.request.ParentRequest;
import com.schoolmgmt.dto.response.BulkStudentResponse;
import com.schoolmgmt.dto.response.StudentResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BulkStudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final UserService userService;
    private final ParentServiceImpl parentService;
    private final ParentRepository parentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;

    /**
     * Create multiple students in bulk
     */
    @Transactional
    public BulkStudentResponse createBulkStudents(BulkStudentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        String batchReference = request.getBatchReference() != null ? 
                request.getBatchReference() : "BULK-" + UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Starting bulk student creation for tenant: {}, batch: {}, total students: {}", 
                tenantId, batchReference, request.getStudents().size());

        // Initialize response builder
        BulkStudentResponse.BulkStudentResponseBuilder responseBuilder = BulkStudentResponse.builder()
                .batchReference(batchReference)
                .totalRequested(request.getStudents().size())
                .processedAt(LocalDateTime.now());

        List<BulkStudentResponse.BulkError> errors = new ArrayList<>();
        List<StudentResponse> createdStudents = new ArrayList<>();
        Map<String, BulkStudentResponse.SectionSummary> sectionSummaries = new HashMap<>();

        // Step 1: Validate all requests first
        List<ValidatedStudentRequest> validatedRequests = validateBulkStudentRequests(
                request.getStudents(), tenantId, errors);

        // Step 2: Group by section for efficient processing
        Map<String, List<ValidatedStudentRequest>> studentsBySection = groupStudentsBySection(validatedRequests);

        // Step 3: Process each section
        for (Map.Entry<String, List<ValidatedStudentRequest>> entry : studentsBySection.entrySet()) {
            String sectionKey = entry.getKey();
            List<ValidatedStudentRequest> sectionStudents = entry.getValue();
            
            try {
                processSectionStudents(sectionStudents, sectionKey, tenantId, 
                        createdStudents, errors, sectionSummaries);
            } catch (Exception e) {
                log.error("Error processing section {}: {}", sectionKey, e.getMessage());
                // Add errors for all students in this section
                for (ValidatedStudentRequest student : sectionStudents) {
                    errors.add(BulkStudentResponse.BulkError.builder()
                            .rowIndex(student.getOriginalIndex())
                            .errorMessage("Section processing failed: " + e.getMessage())
                            .errorType("SECTION_PROCESSING_ERROR")
                            .failedRequest(student.getOriginalRequest())
                            .studentIdentifier(getStudentIdentifier(student.getOriginalRequest()))
                            .build());
                }
            }
        }

        // Build final response
        return responseBuilder
                .successful(createdStudents.size())
                .failed(errors.size())
                .createdStudents(createdStudents)
                .errors(errors)
                .sectionSummaries(new ArrayList<>(sectionSummaries.values()))
                .build();
    }

    /**
     * Validate bulk student data without creating records
     */
    @Transactional(readOnly = true)
    public BulkStudentResponse validateBulkStudents(BulkStudentRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        String batchReference = "VALIDATION-" + UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Validating bulk student data for tenant: {}, total students: {}", 
                tenantId, request.getStudents().size());

        List<BulkStudentResponse.BulkError> errors = new ArrayList<>();
        Map<String, BulkStudentResponse.SectionSummary> sectionSummaries = new HashMap<>();

        // Validate all requests
        List<ValidatedStudentRequest> validatedRequests = validateBulkStudentRequests(
                request.getStudents(), tenantId, errors);

        // Group by section for summary
        Map<String, List<ValidatedStudentRequest>> studentsBySection = groupStudentsBySection(validatedRequests);

        // Create section summaries
        for (Map.Entry<String, List<ValidatedStudentRequest>> entry : studentsBySection.entrySet()) {
            String sectionKey = entry.getKey();
            List<ValidatedStudentRequest> sectionStudents = entry.getValue();
            
            Section section = sectionRepository.findById(UUID.fromString(sectionKey))
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionKey));
            
            sectionSummaries.put(sectionKey, BulkStudentResponse.SectionSummary.builder()
                    .sectionId(sectionKey)
                    .sectionName(section.getName())
                    .totalRequested(sectionStudents.size())
                    .successful(sectionStudents.size())
                    .failed(0)
                    .build());
        }

        return BulkStudentResponse.builder()
                .batchReference(batchReference)
                .totalRequested(request.getStudents().size())
                .successful(validatedRequests.size())
                .failed(errors.size())
                .createdStudents(new ArrayList<>())
                .errors(errors)
                .sectionSummaries(new ArrayList<>(sectionSummaries.values()))
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get bulk upload template with example data
     */
    public BulkStudentRequest getBulkUploadTemplate() {
        // Create example student request
        CreateStudentRequest exampleStudent = CreateStudentRequest.builder()
                .userRequest(com.schoolmgmt.dto.common.UserRequest.builder()
                        .firstName("John")
                        .middleName("Michael")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .phone("+1234567890")
                        .build())
                .dateOfBirth(java.time.LocalDate.of(2010, 5, 15))
                .gender("MALE")
                .address("123 Main Street")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .schoolClass(createExampleSchoolClass())
                .section(createExampleSection())
                .admissionDate(java.time.LocalDate.now())
                .previousSchool("Previous School Name")
                .fatherInfo(ParentInfo.builder()
                        .name("Father Name")
                        .occupation("Engineer")
                        .phone("+1234567891")
                        .email("father@example.com")
                        .build())
                .motherInfo(ParentInfo.builder()
                        .name("Mother Name")
                        .occupation("Teacher")
                        .phone("+1234567892")
                        .email("mother@example.com")
                        .build())
                .emergencyContact(EmergencyContact.builder()
                        .name("Emergency Contact")
                        .relation("Guardian")
                        .phone("+1234567893")
                        .build())
                .createUserAccount(true)
                .build();

        return BulkStudentRequest.builder()
                .students(Arrays.asList(exampleStudent))
                .batchReference("TEMPLATE-EXAMPLE")
                .continueOnError(true)
                .build();
    }

    /**
     * Create example SchoolClass for template
     */
    private SchoolClass createExampleSchoolClass() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(UUID.randomUUID());
        schoolClass.setCode("CLASS-001");
        schoolClass.setName("Grade 10");
        schoolClass.setDescription("Example class for template");
        return schoolClass;
    }

    /**
     * Create example Section for template
     */
    private Section createExampleSection() {
        Section section = new Section();
        section.setId(UUID.randomUUID());
        section.setName("A");
        section.setCapacity(40);
        section.setSchoolClass(createExampleSchoolClass());
        return section;
    }

    /**
     * Validate all student requests before processing
     */
    private List<ValidatedStudentRequest> validateBulkStudentRequests(
            List<CreateStudentRequest> requests, String tenantId, 
            List<BulkStudentResponse.BulkError> errors) {
        
        List<ValidatedStudentRequest> validatedRequests = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            CreateStudentRequest request = requests.get(i);
            
            try {
                // Validate basic request structure
                validateStudentRequest(request, tenantId);
                
                validatedRequests.add(ValidatedStudentRequest.builder()
                        .originalRequest(request)
                        .originalIndex(i)
                        .build());
                        
            } catch (Exception e) {
                errors.add(BulkStudentResponse.BulkError.builder()
                        .rowIndex(i)
                        .errorMessage(e.getMessage())
                        .errorType("VALIDATION_ERROR")
                        .failedRequest(request)
                        .studentIdentifier(getStudentIdentifier(request))
                        .build());
            }
        }
        
        return validatedRequests;
    }

    /**
     * Validate individual student request
     */
    private void validateStudentRequest(CreateStudentRequest request, String tenantId) {
        log.debug("Validating student request for: {}", getStudentIdentifier(request));
        
        // Validate required fields
        if (request.getUserRequest() == null) {
            log.error("Validation failed: User details are required");
            throw new BusinessException("User details are required");
        }
        
        if (request.getUserRequest().getFirstName() == null || 
            request.getUserRequest().getFirstName().trim().isEmpty()) {
            log.error("Validation failed: First name is required");
            throw new BusinessException("First name is required");
        }
        
        if (request.getUserRequest().getLastName() == null || 
            request.getUserRequest().getLastName().trim().isEmpty()) {
            log.error("Validation failed: Last name is required");
            throw new BusinessException("Last name is required");
        }
        
        if (request.getDateOfBirth() == null) {
            log.error("Validation failed: Date of birth is required");
            throw new BusinessException("Date of birth is required");
        }
        
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            log.error("Validation failed: Gender is required");
            throw new BusinessException("Gender is required");
        }
        
        if (request.getSchoolClass() == null || request.getSchoolClass().getId() == null) {
            log.error("Validation failed: Class is required");
            throw new BusinessException("Class is required");
        }
        
        if (request.getSection() == null || request.getSection().getId() == null) {
            log.error("Validation failed: Section is required");
            throw new BusinessException("Section is required");
        }
        
        if (request.getAdmissionDate() == null) {
            log.error("Validation failed: Admission date is required");
            throw new BusinessException("Admission date is required");
        }
        
        if (request.getEmergencyContact() == null || 
            request.getEmergencyContact().getPhone() == null ||
            request.getEmergencyContact().getPhone().trim().isEmpty()) {
            log.error("Validation failed: Emergency contact phone is required");
            throw new BusinessException("Emergency contact phone is required");
        }
        
        // Validate email format if provided
        if (request.getUserRequest().getEmail() != null && 
            !request.getUserRequest().getEmail().trim().isEmpty()) {
            if (!request.getUserRequest().getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                log.error("Validation failed: Invalid email format - {}", request.getUserRequest().getEmail());
                throw new BusinessException("Invalid email format");
            }
        }
        
        // Validate phone format if provided
        if (request.getUserRequest().getPhone() != null && 
            !request.getUserRequest().getPhone().trim().isEmpty()) {
            if (!request.getUserRequest().getPhone().matches("^[+]?[0-9]{10,15}$")) {
                log.error("Validation failed: Invalid phone format - {}", request.getUserRequest().getPhone());
                throw new BusinessException("Invalid phone format");
            }
        }
        
        // Validate parent email formats if provided
        validateParentContactInfo(request.getFatherInfo(), "Father");
        validateParentContactInfo(request.getMotherInfo(), "Mother");
        validateParentContactInfo(request.getGuardianInfo(), "Guardian");
        
        // Validate class and section exist
        SchoolClass schoolClass = schoolClassRepository
                .findByIdAndTenantId(request.getSchoolClass().getId(), tenantId)
                .orElseThrow(() -> {
                    log.error("Validation failed: School class not found - ID: {}, Tenant: {}", 
                            request.getSchoolClass().getId(), tenantId);
                    return new ResourceNotFoundException(
                            "School class not found with ID: " + request.getSchoolClass().getId());
                });
        
        Section section = sectionRepository
                .findByIdAndTenantId(request.getSection().getId(), tenantId)
                .orElseThrow(() -> {
                    log.error("Validation failed: Section not found - ID: {}, Tenant: {}", 
                            request.getSection().getId(), tenantId);
                    return new ResourceNotFoundException(
                            "Section not found with ID: " + request.getSection().getId());
                });
        
        // Check for duplicate student email if provided
        if (request.getUserRequest().getEmail() != null && 
            !request.getUserRequest().getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getUserRequest().getEmail())) {
                log.warn("Validation failed: Student email already registered - {}", request.getUserRequest().getEmail());
                throw new BusinessException("Student email already registered: " + request.getUserRequest().getEmail());
            }
        }
        
        // Check for duplicate student phone if provided
        if (request.getUserRequest().getPhone() != null && 
            !request.getUserRequest().getPhone().trim().isEmpty()) {
            if (userRepository.existsByPhone(request.getUserRequest().getPhone())) {
                log.warn("Validation failed: Student phone already exists - {}", request.getUserRequest().getPhone());
                throw new BusinessException("Student phone number already exists: " + request.getUserRequest().getPhone());
            }
        }
        
        log.debug("Student validation passed for: {}", getStudentIdentifier(request));
    }

    /**
     * Validate parent contact information (allows duplicates as parents can have multiple children)
     */
    private void validateParentContactInfo(ParentInfo parentInfo, String parentType) {
        if (parentInfo == null) {
            return; // Parent info is optional
        }
        
        // Validate email format if provided
        if (parentInfo.getEmail() != null && !parentInfo.getEmail().trim().isEmpty()) {
            if (!parentInfo.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                log.error("Validation failed: Invalid {} email format - {}", parentType, parentInfo.getEmail());
                throw new BusinessException("Invalid " + parentType.toLowerCase() + " email format");
            }
            // Note: We DON'T check for duplicate parent email/phone here as parents can have multiple children
        }
        
        // Validate phone format if provided
        if (parentInfo.getPhone() != null && !parentInfo.getPhone().trim().isEmpty()) {
            if (!parentInfo.getPhone().matches("^[+]?[0-9]{10,15}$")) {
                log.error("Validation failed: Invalid {} phone format - {}", parentType, parentInfo.getPhone());
                throw new BusinessException("Invalid " + parentType.toLowerCase() + " phone format");
            }
            // Note: We DON'T check for duplicate parent phone here as parents can have multiple children
        }
    }

    /**
     * Group students by section for efficient processing
     */
    private Map<String, List<ValidatedStudentRequest>> groupStudentsBySection(
            List<ValidatedStudentRequest> validatedRequests) {
        
        Map<String, List<ValidatedStudentRequest>> groupedStudents = new HashMap<>();
        
        for (ValidatedStudentRequest validatedRequest : validatedRequests) {
            String sectionKey = validatedRequest.getOriginalRequest().getSection().getId().toString();
            groupedStudents.computeIfAbsent(sectionKey, k -> new ArrayList<>()).add(validatedRequest);
        }
        
        return groupedStudents;
    }

    /**
     * Process students for a specific section
     */
    private void processSectionStudents(
            List<ValidatedStudentRequest> sectionStudents, String sectionKey, String tenantId,
            List<StudentResponse> createdStudents, List<BulkStudentResponse.BulkError> errors,
            Map<String, BulkStudentResponse.SectionSummary> sectionSummaries) {
        
        int successfulInSection = 0;
        int failedInSection = 0;
        
        log.info("Processing {} students for section: {}", sectionStudents.size(), sectionKey);
        
        // Get section details for summary
        Section section = sectionRepository.findById(UUID.fromString(sectionKey))
                .orElseThrow(() -> {
                    log.error("Section not found during processing: {}", sectionKey);
                    return new ResourceNotFoundException("Section not found: " + sectionKey);
                });
        
        for (ValidatedStudentRequest validatedStudent : sectionStudents) {
            String studentIdentifier = getStudentIdentifier(validatedStudent.getOriginalRequest());
            int rowIndex = validatedStudent.getOriginalIndex();
            
            try {
                log.debug("Creating student: {} (row: {}) in section: {}", studentIdentifier, rowIndex, sectionKey);
                
                // Create student using existing single student creation logic
                StudentResponse studentResponse = studentService.createStudent(validatedStudent.getOriginalRequest());
                createdStudents.add(studentResponse);
                successfulInSection++;
                
                log.info("Successfully created student: {} (ID: {}) for section: {}", 
                        studentResponse.getFullName(), studentResponse.getId(), sectionKey);
                        
            } catch (BusinessException e) {
                failedInSection++;
                String errorType = determineErrorType(e.getMessage());
                
                errors.add(BulkStudentResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .errorMessage(e.getMessage())
                        .errorType(errorType)
                        .failedRequest(validatedStudent.getOriginalRequest())
                        .studentIdentifier(studentIdentifier)
                        .build());
                
                log.warn("Business error creating student {} (row: {}): {}", 
                        studentIdentifier, rowIndex, e.getMessage());
                        
            } catch (ResourceNotFoundException e) {
                failedInSection++;
                
                errors.add(BulkStudentResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .errorMessage(e.getMessage())
                        .errorType("RESOURCE_NOT_FOUND")
                        .failedRequest(validatedStudent.getOriginalRequest())
                        .studentIdentifier(studentIdentifier)
                        .build());
                
                log.error("Resource not found error creating student {} (row: {}): {}", 
                        studentIdentifier, rowIndex, e.getMessage());
                        
            } catch (Exception e) {
                failedInSection++;
                
                errors.add(BulkStudentResponse.BulkError.builder()
                        .rowIndex(rowIndex)
                        .errorMessage("Unexpected error: " + e.getMessage())
                        .errorType("SYSTEM_ERROR")
                        .failedRequest(validatedStudent.getOriginalRequest())
                        .studentIdentifier(studentIdentifier)
                        .build());
                
                log.error("Unexpected error creating student {} (row: {}): {}", 
                        studentIdentifier, rowIndex, e.getMessage(), e);
            }
        }
        
        // Add section summary
        BulkStudentResponse.SectionSummary summary = BulkStudentResponse.SectionSummary.builder()
                .sectionId(sectionKey)
                .sectionName(section.getName())
                .totalRequested(sectionStudents.size())
                .successful(successfulInSection)
                .failed(failedInSection)
                .build();
        
        sectionSummaries.put(sectionKey, summary);
        
        log.info("Section processing completed for {}: {} successful, {} failed", 
                sectionKey, successfulInSection, failedInSection);
    }

    /**
     * Determine error type based on error message for better categorization
     */
    private String determineErrorType(String errorMessage) {
        if (errorMessage == null) {
            return "UNKNOWN_ERROR";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("email") && lowerMessage.contains("already")) {
            return "DUPLICATE_EMAIL";
        } else if (lowerMessage.contains("phone") && lowerMessage.contains("already")) {
            return "DUPLICATE_PHONE";
        } else if (lowerMessage.contains("class") && lowerMessage.contains("not found")) {
            return "CLASS_NOT_FOUND";
        } else if (lowerMessage.contains("section") && lowerMessage.contains("not found")) {
            return "SECTION_NOT_FOUND";
        } else if (lowerMessage.contains("required")) {
            return "MISSING_REQUIRED_FIELD";
        } else if (lowerMessage.contains("invalid") && lowerMessage.contains("format")) {
            return "INVALID_FORMAT";
        } else if (lowerMessage.contains("roll number") && lowerMessage.contains("already")) {
            return "DUPLICATE_ROLL_NUMBER";
        } else {
            return "BUSINESS_ERROR";
        }
    }

    /**
     * Get student identifier for error reporting
     */
    private String getStudentIdentifier(CreateStudentRequest request) {
        if (request.getUserRequest() != null) {
            String name = "";
            if (request.getUserRequest().getFirstName() != null) {
                name += request.getUserRequest().getFirstName();
            }
            if (request.getUserRequest().getLastName() != null) {
                name += " " + request.getUserRequest().getLastName();
            }
            return name.trim();
        }
        return "Unknown Student";
    }

    /**
     * Inner class to hold validated request with original index
     */
    private static class ValidatedStudentRequest {
        private CreateStudentRequest originalRequest;
        private int originalIndex;
        
        public ValidatedStudentRequest() {}
        
        public ValidatedStudentRequest(CreateStudentRequest originalRequest, int originalIndex) {
            this.originalRequest = originalRequest;
            this.originalIndex = originalIndex;
        }
        
        public CreateStudentRequest getOriginalRequest() {
            return originalRequest;
        }
        
        public void setOriginalRequest(CreateStudentRequest originalRequest) {
            this.originalRequest = originalRequest;
        }
        
        public int getOriginalIndex() {
            return originalIndex;
        }
        
        public void setOriginalIndex(int originalIndex) {
            this.originalIndex = originalIndex;
        }
        
        public static ValidatedStudentRequestBuilder builder() {
            return new ValidatedStudentRequestBuilder();
        }
        
        public static class ValidatedStudentRequestBuilder {
            private CreateStudentRequest originalRequest;
            private int originalIndex;
            
            public ValidatedStudentRequestBuilder originalRequest(CreateStudentRequest originalRequest) {
                this.originalRequest = originalRequest;
                return this;
            }
            
            public ValidatedStudentRequestBuilder originalIndex(int originalIndex) {
                this.originalIndex = originalIndex;
                return this;
            }
            
            public ValidatedStudentRequest build() {
                return new ValidatedStudentRequest(originalRequest, originalIndex);
            }
        }
    }
}
