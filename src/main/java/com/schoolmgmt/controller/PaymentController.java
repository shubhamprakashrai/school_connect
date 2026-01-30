package com.schoolmgmt.controller;

import com.schoolmgmt.model.PaymentTransaction;
import com.schoolmgmt.service.PaymentTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payments", description = "Payment gateway integration APIs")
public class PaymentController {

    private final PaymentTransactionService service;

    @PostMapping
    @Operation(summary = "Create payment transaction")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PARENT', 'STUDENT')")
    public ResponseEntity<PaymentTransaction> create(@RequestBody PaymentTransaction transaction) {
        log.info("Creating payment transaction amount: {}", transaction.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(transaction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PARENT', 'STUDENT')")
    public ResponseEntity<PaymentTransaction> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<PaymentTransaction>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get payments by student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PARENT', 'STUDENT')")
    public ResponseEntity<List<PaymentTransaction>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(service.getByStudentId(studentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentTransaction> update(@PathVariable UUID id, @RequestBody PaymentTransaction transaction) {
        return ResponseEntity.ok(service.update(id, transaction));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment record")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
