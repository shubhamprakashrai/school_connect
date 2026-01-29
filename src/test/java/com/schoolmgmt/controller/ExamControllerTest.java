package com.schoolmgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.model.Exam;
import com.schoolmgmt.model.ExamResult;
import com.schoolmgmt.model.ExamType;
import com.schoolmgmt.security.JwtAuthenticationEntryPoint;
import com.schoolmgmt.security.JwtAuthenticationFilter;
import com.schoolmgmt.security.JwtService;
import com.schoolmgmt.service.ExamService;
import com.schoolmgmt.util.TenantCleanupFilter;
import com.schoolmgmt.util.TenantInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExamController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExamService examService;

    // Security infrastructure beans required by SecurityConfig
    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private TenantInterceptor tenantInterceptor;

    @MockBean
    private TenantCleanupFilter tenantCleanupFilter;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private ExamType sampleExamType;
    private Exam sampleExam;
    private ExamResult sampleResult;
    private UUID examTypeId;
    private UUID examId;
    private UUID studentId;
    private UUID classId;

    @BeforeEach
    void setUp() throws Exception {
        // Allow interceptor to pass requests through
        when(tenantInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        examTypeId = UUID.randomUUID();
        examId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        classId = UUID.randomUUID();

        sampleExamType = ExamType.builder()
                .id(examTypeId)
                .tenantId("tenant-001")
                .name("Mid-Term")
                .description("Mid-term examination")
                .weightage(30.0)
                .maxMarks(100)
                .passingMarks(33)
                .isActive(true)
                .displayOrder(1)
                .build();

        sampleExam = Exam.builder()
                .id(examId)
                .tenantId("tenant-001")
                .name("Mathematics Mid-Term")
                .description("Mid-term exam for Mathematics")
                .examType(sampleExamType)
                .classId(classId)
                .section("A")
                .subjectName("Mathematics")
                .examDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .maxMarks(100)
                .passingMarks(33)
                .room("Hall-1")
                .status(Exam.ExamStatus.SCHEDULED)
                .academicYear("2025-2026")
                .build();

        sampleResult = ExamResult.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .exam(sampleExam)
                .studentId(studentId)
                .studentName("John Doe")
                .marksObtained(85.0)
                .maxMarks(100)
                .percentage(85.0)
                .grade("A")
                .resultStatus(ExamResult.ResultStatus.PASS)
                .isAbsent(false)
                .build();
    }

    // =========================================================================
    // GET /api/exams/types - List exam types
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams/types - should return list of exam types")
    @WithMockUser(roles = "ADMIN")
    void getExamTypes_shouldReturnListOfExamTypes() throws Exception {
        ExamType unitTest = ExamType.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .name("Unit Test")
                .description("Unit test examination")
                .maxMarks(50)
                .passingMarks(17)
                .isActive(true)
                .build();

        when(examService.getExamTypes()).thenReturn(List.of(sampleExamType, unitTest));

        mockMvc.perform(get("/api/exams/types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Mid-Term")))
                .andExpect(jsonPath("$[1].name", is("Unit Test")));

        verify(examService, times(1)).getExamTypes();
    }

    @Test
    @DisplayName("GET /api/exams/types - should return empty list when no exam types exist")
    @WithMockUser(roles = "TEACHER")
    void getExamTypes_shouldReturnEmptyList() throws Exception {
        when(examService.getExamTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/exams/types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(examService, times(1)).getExamTypes();
    }

    // =========================================================================
    // POST /api/exams/types - Create exam type
    // =========================================================================

    @Test
    @DisplayName("POST /api/exams/types - should create a new exam type and return 201")
    @WithMockUser(roles = "ADMIN")
    void createExamType_shouldCreateAndReturn201() throws Exception {
        ExamType inputType = ExamType.builder()
                .name("Final Exam")
                .description("Final examination")
                .maxMarks(100)
                .passingMarks(33)
                .weightage(40.0)
                .build();

        ExamType createdType = ExamType.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .name("Final Exam")
                .description("Final examination")
                .maxMarks(100)
                .passingMarks(33)
                .weightage(40.0)
                .isActive(true)
                .build();

        when(examService.createExamType(any(ExamType.class))).thenReturn(createdType);

        mockMvc.perform(post("/api/exams/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputType)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Final Exam")))
                .andExpect(jsonPath("$.description", is("Final examination")))
                .andExpect(jsonPath("$.maxMarks", is(100)))
                .andExpect(jsonPath("$.id").exists());

        verify(examService, times(1)).createExamType(any(ExamType.class));
    }

    // =========================================================================
    // GET /api/exams - List exams (paginated)
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams - should return paginated list of exams")
    @WithMockUser(roles = "ADMIN")
    void getExams_shouldReturnPaginatedExams() throws Exception {
        // Build a simple exam without lazy-loaded ExamType to avoid serialization issues
        Exam pageExam = Exam.builder()
                .id(examId)
                .tenantId("tenant-001")
                .name("Mathematics Mid-Term")
                .examDate(LocalDate.now().plusDays(7))
                .maxMarks(100)
                .passingMarks(33)
                .status(Exam.ExamStatus.SCHEDULED)
                .classId(classId)
                .build();

        Page<Exam> examPage = new PageImpl<>(
                new java.util.ArrayList<>(List.of(pageExam)),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );

        when(examService.getExams(any(Pageable.class))).thenReturn(examPage);

        mockMvc.perform(get("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Mathematics Mid-Term")))
                .andExpect(jsonPath("$.content[0].status", is("SCHEDULED")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(examService, times(1)).getExams(any(Pageable.class));
    }

    // =========================================================================
    // GET /api/exams/upcoming - Upcoming exams
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams/upcoming - should return upcoming exams")
    @WithMockUser(roles = "STUDENT")
    void getUpcomingExams_shouldReturnUpcomingExams() throws Exception {
        Exam upcomingExam = Exam.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .name("Science Final")
                .examDate(LocalDate.now().plusDays(14))
                .maxMarks(100)
                .passingMarks(33)
                .status(Exam.ExamStatus.SCHEDULED)
                .classId(classId)
                .examType(sampleExamType)
                .build();

        when(examService.getUpcomingExams()).thenReturn(List.of(sampleExam, upcomingExam));

        mockMvc.perform(get("/api/exams/upcoming")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Mathematics Mid-Term")))
                .andExpect(jsonPath("$[1].name", is("Science Final")));

        verify(examService, times(1)).getUpcomingExams();
    }

    @Test
    @DisplayName("GET /api/exams/upcoming - should return empty list when no upcoming exams")
    @WithMockUser(roles = "TEACHER")
    void getUpcomingExams_shouldReturnEmptyWhenNone() throws Exception {
        when(examService.getUpcomingExams()).thenReturn(List.of());

        mockMvc.perform(get("/api/exams/upcoming")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(examService, times(1)).getUpcomingExams();
    }

    // =========================================================================
    // POST /api/exams/{examId}/marks - Enter marks
    // =========================================================================

    @Test
    @DisplayName("POST /api/exams/{examId}/marks - should enter marks and return 201")
    @WithMockUser(roles = "TEACHER")
    void enterMarks_shouldCreateResultAndReturn201() throws Exception {
        ExamResult inputResult = ExamResult.builder()
                .studentId(studentId)
                .studentName("John Doe")
                .marksObtained(85.0)
                .maxMarks(100)
                .isAbsent(false)
                .build();

        when(examService.getExamById(examId)).thenReturn(sampleExam);
        when(examService.enterMarks(any(ExamResult.class))).thenReturn(sampleResult);

        mockMvc.perform(post("/api/exams/{examId}/marks", examId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputResult)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName", is("John Doe")))
                .andExpect(jsonPath("$.marksObtained", is(85.0)))
                .andExpect(jsonPath("$.grade", is("A")))
                .andExpect(jsonPath("$.resultStatus", is("PASS")));

        verify(examService, times(1)).getExamById(examId);
        verify(examService, times(1)).enterMarks(any(ExamResult.class));
    }

    // =========================================================================
    // GET /api/exams/{examId}/results - Get exam results
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams/{examId}/results - should return exam results")
    @WithMockUser(roles = "ADMIN")
    void getExamResults_shouldReturnResults() throws Exception {
        ExamResult secondResult = ExamResult.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .exam(sampleExam)
                .studentId(UUID.randomUUID())
                .studentName("Jane Smith")
                .marksObtained(72.0)
                .maxMarks(100)
                .percentage(72.0)
                .grade("B+")
                .resultStatus(ExamResult.ResultStatus.PASS)
                .isAbsent(false)
                .build();

        when(examService.getResultsByExam(examId)).thenReturn(List.of(sampleResult, secondResult));

        mockMvc.perform(get("/api/exams/{examId}/results", examId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].studentName", is("John Doe")))
                .andExpect(jsonPath("$[0].marksObtained", is(85.0)))
                .andExpect(jsonPath("$[1].studentName", is("Jane Smith")))
                .andExpect(jsonPath("$[1].grade", is("B+")));

        verify(examService, times(1)).getResultsByExam(examId);
    }

    // =========================================================================
    // GET /api/exams/{examId}/statistics - Get exam statistics
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams/{examId}/statistics - should return exam statistics")
    @WithMockUser(roles = "TEACHER")
    void getExamStatistics_shouldReturnStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", 30);
        stats.put("averagePercentage", 72.5);
        stats.put("passed", 25L);
        stats.put("failed", 5L);
        stats.put("passPercentage", 83.33);
        stats.put("highestMarks", 98.0);
        stats.put("lowestMarks", 15.0);
        stats.put("topper", "John Doe");

        when(examService.getExamStatistics(examId)).thenReturn(stats);

        mockMvc.perform(get("/api/exams/{examId}/statistics", examId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents", is(30)))
                .andExpect(jsonPath("$.averagePercentage", is(72.5)))
                .andExpect(jsonPath("$.passed", is(25)))
                .andExpect(jsonPath("$.failed", is(5)))
                .andExpect(jsonPath("$.passPercentage", is(83.33)))
                .andExpect(jsonPath("$.highestMarks", is(98.0)))
                .andExpect(jsonPath("$.topper", is("John Doe")));

        verify(examService, times(1)).getExamStatistics(examId);
    }

    // =========================================================================
    // GET /api/exams/{id} - Get exam by ID
    // =========================================================================

    @Test
    @DisplayName("GET /api/exams/{id} - should return exam details by ID")
    @WithMockUser(roles = "ADMIN")
    void getExamById_shouldReturnExamDetails() throws Exception {
        when(examService.getExamById(examId)).thenReturn(sampleExam);

        mockMvc.perform(get("/api/exams/{id}", examId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mathematics Mid-Term")))
                .andExpect(jsonPath("$.status", is("SCHEDULED")))
                .andExpect(jsonPath("$.maxMarks", is(100)))
                .andExpect(jsonPath("$.room", is("Hall-1")))
                .andExpect(jsonPath("$.section", is("A")));

        verify(examService, times(1)).getExamById(examId);
    }

    // =========================================================================
    // DELETE /api/exams/types/{id} - Delete exam type
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/exams/types/{id} - should delete exam type and return 204")
    @WithMockUser(roles = "ADMIN")
    void deleteExamType_shouldReturn204() throws Exception {
        doNothing().when(examService).deleteExamType(examTypeId);

        mockMvc.perform(delete("/api/exams/types/{id}", examTypeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(examService, times(1)).deleteExamType(examTypeId);
    }
}
