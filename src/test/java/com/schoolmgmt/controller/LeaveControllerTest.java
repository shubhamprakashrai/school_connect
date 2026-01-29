package com.schoolmgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.model.LeaveBalance;
import com.schoolmgmt.model.LeaveRequest;
import com.schoolmgmt.model.LeaveType;
import com.schoolmgmt.model.User;
import com.schoolmgmt.security.JwtAuthenticationEntryPoint;
import com.schoolmgmt.security.JwtAuthenticationFilter;
import com.schoolmgmt.security.JwtService;
import com.schoolmgmt.service.LeaveService;
import com.schoolmgmt.util.TenantCleanupFilter;
import com.schoolmgmt.util.TenantInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

@WebMvcTest(LeaveController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveService leaveService;

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

    private LeaveType sickLeaveType;
    private LeaveType casualLeaveType;
    private LeaveRequest sampleLeaveRequest;
    private User mockUser;
    private UUID leaveRequestId;
    private UUID sickLeaveTypeId;
    private UUID casualLeaveTypeId;

    @BeforeEach
    void setUp() throws Exception {
        // Allow interceptor to pass requests through
        when(tenantInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        sickLeaveTypeId = UUID.randomUUID();
        casualLeaveTypeId = UUID.randomUUID();
        leaveRequestId = UUID.randomUUID();

        sickLeaveType = LeaveType.builder()
                .id(sickLeaveTypeId)
                .tenantId("tenant-001")
                .name("SICK")
                .description("Sick Leave")
                .maxDaysPerYear(12)
                .isPaid(true)
                .requiresApproval(true)
                .isActive(true)
                .build();

        casualLeaveType = LeaveType.builder()
                .id(casualLeaveTypeId)
                .tenantId("tenant-001")
                .name("CASUAL")
                .description("Casual Leave")
                .maxDaysPerYear(10)
                .isPaid(true)
                .requiresApproval(true)
                .isActive(true)
                .build();

        sampleLeaveRequest = LeaveRequest.builder()
                .id(leaveRequestId)
                .tenantId("tenant-001")
                .leaveType(sickLeaveType)
                .userId("user-001")
                .userName("Jane Teacher")
                .userRole("TEACHER")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .totalDays(3)
                .reason("Medical appointment")
                .status(LeaveRequest.LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        mockUser = User.builder()
                .email("teacher@school.com")
                .password("encoded-password")
                .firstName("Jane")
                .lastName("Teacher")
                .role(User.UserRole.ADMIN)
                .isActive(true)
                .userId("user-001")
                .build();

        // Set SecurityContext so @AuthenticationPrincipal resolves the User
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // GET /api/leave/types/active - List active leave types
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/types/active - should return active leave types")
    void getActiveLeaveTypes_shouldReturnActiveTypes() throws Exception {
        when(leaveService.getActiveLeaveTypes()).thenReturn(List.of(sickLeaveType, casualLeaveType));

        mockMvc.perform(get("/api/leave/types/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("SICK")))
                .andExpect(jsonPath("$[0].maxDaysPerYear", is(12)))
                .andExpect(jsonPath("$[1].name", is("CASUAL")))
                .andExpect(jsonPath("$[1].maxDaysPerYear", is(10)));

        verify(leaveService, times(1)).getActiveLeaveTypes();
    }

    @Test
    @DisplayName("GET /api/leave/types/active - should return empty list when no active types")
    void getActiveLeaveTypes_shouldReturnEmptyWhenNone() throws Exception {
        when(leaveService.getActiveLeaveTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/leave/types/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(leaveService, times(1)).getActiveLeaveTypes();
    }

    // =========================================================================
    // POST /api/leave/request - Apply leave
    // =========================================================================

    @Test
    @DisplayName("POST /api/leave/request - should apply leave and return 201")
    void applyLeave_shouldCreateAndReturn201() throws Exception {
        LeaveRequest inputRequest = LeaveRequest.builder()
                .leaveType(sickLeaveType)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .reason("Medical appointment")
                .build();

        when(leaveService.applyLeave(any(LeaveRequest.class))).thenReturn(sampleLeaveRequest);

        mockMvc.perform(post("/api/leave/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId", is("user-001")))
                .andExpect(jsonPath("$.userName", is("Jane Teacher")))
                .andExpect(jsonPath("$.reason", is("Medical appointment")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalDays", is(3)));

        verify(leaveService, times(1)).applyLeave(any(LeaveRequest.class));
    }

    // =========================================================================
    // GET /api/leave/my - Get my leaves
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/my - should return leaves for authenticated user")
    void getMyLeaves_shouldReturnUserLeaves() throws Exception {
        LeaveRequest secondRequest = LeaveRequest.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .leaveType(casualLeaveType)
                .userId("user-001")
                .userName("Jane Teacher")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .totalDays(2)
                .reason("Family event")
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .build();

        when(leaveService.getMyLeaves("user-001")).thenReturn(List.of(sampleLeaveRequest, secondRequest));

        mockMvc.perform(get("/api/leave/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].status", is("APPROVED")))
                .andExpect(jsonPath("$[1].reason", is("Family event")));

        verify(leaveService, times(1)).getMyLeaves("user-001");
    }

    // =========================================================================
    // GET /api/leave/requests/pending - Get pending approvals
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/requests/pending - should return pending leave requests")
    void getPendingApprovals_shouldReturnPendingRequests() throws Exception {
        when(leaveService.getPendingApprovals()).thenReturn(List.of(sampleLeaveRequest));

        mockMvc.perform(get("/api/leave/requests/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[0].userName", is("Jane Teacher")))
                .andExpect(jsonPath("$[0].reason", is("Medical appointment")));

        verify(leaveService, times(1)).getPendingApprovals();
    }

    // =========================================================================
    // PUT /api/leave/requests/{id}/approve - Approve leave
    // =========================================================================

    @Test
    @DisplayName("PUT /api/leave/requests/{id}/approve - should approve leave request")
    void approveLeave_shouldApproveAndReturnUpdatedRequest() throws Exception {
        LeaveRequest approvedRequest = LeaveRequest.builder()
                .id(leaveRequestId)
                .tenantId("tenant-001")
                .leaveType(sickLeaveType)
                .userId("user-001")
                .userName("Jane Teacher")
                .startDate(sampleLeaveRequest.getStartDate())
                .endDate(sampleLeaveRequest.getEndDate())
                .totalDays(3)
                .reason("Medical appointment")
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy("user-001")
                .approvedByName("Jane Teacher")
                .approvalRemarks("Approved for medical reasons")
                .approvedAt(LocalDateTime.now())
                .build();

        when(leaveService.approveLeave(eq(leaveRequestId), eq("user-001"),
                eq("Jane Teacher"), eq("Approved for medical reasons")))
                .thenReturn(approvedRequest);

        Map<String, String> body = new HashMap<>();
        body.put("remarks", "Approved for medical reasons");

        mockMvc.perform(put("/api/leave/requests/{id}/approve", leaveRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approvedBy", is("user-001")))
                .andExpect(jsonPath("$.approvedByName", is("Jane Teacher")))
                .andExpect(jsonPath("$.approvalRemarks", is("Approved for medical reasons")));

        verify(leaveService, times(1)).approveLeave(
                eq(leaveRequestId), eq("user-001"), eq("Jane Teacher"),
                eq("Approved for medical reasons"));
    }

    // =========================================================================
    // PUT /api/leave/requests/{id}/reject - Reject leave
    // =========================================================================

    @Test
    @DisplayName("PUT /api/leave/requests/{id}/reject - should reject leave request")
    void rejectLeave_shouldRejectAndReturnUpdatedRequest() throws Exception {
        LeaveRequest rejectedRequest = LeaveRequest.builder()
                .id(leaveRequestId)
                .tenantId("tenant-001")
                .leaveType(sickLeaveType)
                .userId("user-001")
                .userName("Jane Teacher")
                .startDate(sampleLeaveRequest.getStartDate())
                .endDate(sampleLeaveRequest.getEndDate())
                .totalDays(3)
                .reason("Medical appointment")
                .status(LeaveRequest.LeaveStatus.REJECTED)
                .approvedBy("user-001")
                .approvedByName("Jane Teacher")
                .approvalRemarks("Insufficient coverage")
                .approvedAt(LocalDateTime.now())
                .build();

        when(leaveService.rejectLeave(eq(leaveRequestId), eq("user-001"),
                eq("Jane Teacher"), eq("Insufficient coverage")))
                .thenReturn(rejectedRequest);

        Map<String, String> body = new HashMap<>();
        body.put("remarks", "Insufficient coverage");

        mockMvc.perform(put("/api/leave/requests/{id}/reject", leaveRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.approvedBy", is("user-001")))
                .andExpect(jsonPath("$.approvalRemarks", is("Insufficient coverage")));

        verify(leaveService, times(1)).rejectLeave(
                eq(leaveRequestId), eq("user-001"), eq("Jane Teacher"),
                eq("Insufficient coverage"));
    }

    // =========================================================================
    // GET /api/leave/balance - Get leave balance
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/balance - should return leave balance for authenticated user")
    void getMyBalance_shouldReturnLeaveBalances() throws Exception {
        LeaveBalance sickBalance = LeaveBalance.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .userId("user-001")
                .leaveType(sickLeaveType)
                .academicYear("2025-2026")
                .totalAllocated(12)
                .used(3)
                .pending(1)
                .build();

        LeaveBalance casualBalance = LeaveBalance.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-001")
                .userId("user-001")
                .leaveType(casualLeaveType)
                .academicYear("2025-2026")
                .totalAllocated(10)
                .used(2)
                .pending(0)
                .build();

        when(leaveService.getLeaveBalance(eq("user-001"), anyString()))
                .thenReturn(List.of(sickBalance, casualBalance));

        mockMvc.perform(get("/api/leave/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].totalAllocated", is(12)))
                .andExpect(jsonPath("$[0].used", is(3)))
                .andExpect(jsonPath("$[0].pending", is(1)))
                .andExpect(jsonPath("$[1].totalAllocated", is(10)))
                .andExpect(jsonPath("$[1].used", is(2)));

        verify(leaveService, times(1)).getLeaveBalance(eq("user-001"), anyString());
    }

    // =========================================================================
    // GET /api/leave/summary - Get leave summary
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/summary - should return leave summary for authenticated user")
    void getMySummary_shouldReturnLeaveSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAllocated", 22);
        summary.put("totalUsed", 5);
        summary.put("totalPending", 1);
        summary.put("totalRemaining", 16);
        summary.put("pendingRequests", 1);
        summary.put("academicYear", "2025-2026");

        when(leaveService.getLeaveSummary("user-001")).thenReturn(summary);

        mockMvc.perform(get("/api/leave/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAllocated", is(22)))
                .andExpect(jsonPath("$.totalUsed", is(5)))
                .andExpect(jsonPath("$.totalPending", is(1)))
                .andExpect(jsonPath("$.totalRemaining", is(16)))
                .andExpect(jsonPath("$.pendingRequests", is(1)))
                .andExpect(jsonPath("$.academicYear", is("2025-2026")));

        verify(leaveService, times(1)).getLeaveSummary("user-001");
    }

    // =========================================================================
    // GET /api/leave/balance with academicYear param
    // =========================================================================

    @Test
    @DisplayName("GET /api/leave/balance?academicYear=2024-2025 - should pass academic year param")
    void getMyBalance_withAcademicYearParam_shouldPassParam() throws Exception {
        when(leaveService.getLeaveBalance("user-001", "2024-2025")).thenReturn(List.of());

        mockMvc.perform(get("/api/leave/balance")
                        .param("academicYear", "2024-2025")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(leaveService, times(1)).getLeaveBalance("user-001", "2024-2025");
    }
}
