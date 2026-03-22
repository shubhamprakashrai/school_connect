package com.schoolmgmt.controller;

import com.schoolmgmt.service.ExamService;
import com.schoolmgmt.service.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports & PDF Generation", description = "APIs for generating report cards, fee receipts, and other PDF documents")
public class ReportController {

    private final PdfGenerationService pdfGenerationService;
    private final ExamService examService;

    // ===== Report Card Endpoints =====

    @GetMapping("/student/{studentId}/report-card")
    @Operation(summary = "Get student report card",
               description = "Returns report card as PDF (default) or JSON based on format parameter")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<?> getReportCard(
            @PathVariable String studentId,
            @RequestParam(value = "academicYearId", required = false) UUID academicYearId,
            @RequestParam(value = "format", defaultValue = "pdf") String format) {

        log.info("Generating report card for student: {}, format: {}", studentId, format);

        if ("json".equalsIgnoreCase(format)) {
            UUID studentUUID = UUID.fromString(studentId);
            Map<String, Object> reportData = examService.generateReportCard(studentUUID);
            return ResponseEntity.ok(reportData);
        }

        // Default: PDF
        byte[] pdfBytes = pdfGenerationService.generateReportCard(studentId, academicYearId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report-card-" + studentId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // ===== Fee Receipt Endpoints =====

    @GetMapping("/fee-receipt/{paymentId}")
    @Operation(summary = "Generate fee receipt PDF",
               description = "Generates and downloads a fee receipt as PDF for the given payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<byte[]> getFeeReceipt(@PathVariable UUID paymentId) {

        log.info("Generating fee receipt PDF for payment: {}", paymentId);

        byte[] pdfBytes = pdfGenerationService.generateFeeReceipt(paymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "fee-receipt-" + paymentId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
