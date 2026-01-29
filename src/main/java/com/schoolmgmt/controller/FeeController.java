package com.schoolmgmt.controller;

import com.schoolmgmt.model.FeePayment;
import com.schoolmgmt.model.FeeStructure;
import com.schoolmgmt.model.FeeType;
import com.schoolmgmt.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fee Management", description = "APIs for managing fees, payments, and receipts")
public class FeeController {

    private final FeeService feeService;

    // ===== Fee Type Endpoints =====

    @PostMapping("/types")
    @Operation(summary = "Create fee type", description = "Create a new fee type (e.g., Tuition, Transport)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FeeType> createFeeType(@Valid @RequestBody FeeType feeType) {
        log.info("Creating fee type: {}", feeType.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(feeService.createFeeType(feeType));
    }

    @GetMapping("/types")
    @Operation(summary = "Get fee types", description = "Get all fee types")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<FeeType>> getFeeTypes() {
        return ResponseEntity.ok(feeService.getFeeTypes());
    }

    @GetMapping("/types/active")
    @Operation(summary = "Get active fee types", description = "Get only active fee types")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<FeeType>> getActiveFeeTypes() {
        return ResponseEntity.ok(feeService.getActiveFeeTypes());
    }

    @PutMapping("/types/{id}")
    @Operation(summary = "Update fee type", description = "Update a fee type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FeeType> updateFeeType(@PathVariable UUID id, @Valid @RequestBody FeeType feeType) {
        return ResponseEntity.ok(feeService.updateFeeType(id, feeType));
    }

    @DeleteMapping("/types/{id}")
    @Operation(summary = "Delete fee type", description = "Delete a fee type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFeeType(@PathVariable UUID id) {
        feeService.deleteFeeType(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Fee Structure Endpoints =====

    @PostMapping("/structure")
    @Operation(summary = "Create fee structure", description = "Create a fee structure for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FeeStructure> createFeeStructure(@Valid @RequestBody FeeStructure structure) {
        log.info("Creating fee structure for class: {}", structure.getClassId());
        return ResponseEntity.status(HttpStatus.CREATED).body(feeService.createFeeStructure(structure));
    }

    @GetMapping("/structure")
    @Operation(summary = "Get all fee structures", description = "Get all active fee structures")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<FeeStructure>> getFeeStructures() {
        return ResponseEntity.ok(feeService.getFeeStructures());
    }

    @GetMapping("/structure/class/{classId}")
    @Operation(summary = "Get fee structures by class", description = "Get fee structures for a class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<FeeStructure>> getFeeStructuresByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(feeService.getFeeStructuresByClass(classId));
    }

    @PutMapping("/structure/{id}")
    @Operation(summary = "Update fee structure", description = "Update a fee structure")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FeeStructure> updateFeeStructure(
            @PathVariable UUID id, @Valid @RequestBody FeeStructure structure) {
        return ResponseEntity.ok(feeService.updateFeeStructure(id, structure));
    }

    // ===== Payment Endpoints =====

    @PostMapping("/payment")
    @Operation(summary = "Collect fee", description = "Record a fee payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<FeePayment> collectFee(@Valid @RequestBody FeePayment payment) {
        log.info("Collecting fee for student: {}", payment.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(feeService.collectFee(payment));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student payments", description = "Get all payments for a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<FeePayment>> getStudentPayments(@PathVariable UUID studentId) {
        return ResponseEntity.ok(feeService.getStudentPayments(studentId));
    }

    @GetMapping("/student/{studentId}/pending")
    @Operation(summary = "Get student pending fees", description = "Get pending fees for a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<FeePayment>> getStudentPendingFees(@PathVariable UUID studentId) {
        return ResponseEntity.ok(feeService.getStudentPendingFees(studentId));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments", description = "Get paginated list of all payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<FeePayment>> getAllPayments(
            @PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(feeService.getAllPayments(pageable));
    }

    @GetMapping("/receipt/{paymentId}")
    @Operation(summary = "Get payment receipt", description = "Get payment details for receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<FeePayment> getReceipt(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(feeService.getPaymentById(paymentId));
    }

    @GetMapping("/report/collection")
    @Operation(summary = "Get collection report", description = "Get fee collection summary report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCollectionReport() {
        return ResponseEntity.ok(feeService.getCollectionReport());
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue payments", description = "Get all overdue payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<FeePayment>> getOverduePayments() {
        return ResponseEntity.ok(feeService.getOverduePayments());
    }
}
