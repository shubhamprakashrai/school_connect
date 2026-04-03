package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.AdjustInvoiceRequest;
import com.schoolmgmt.dto.request.AssignSubscriptionRequest;
import com.schoolmgmt.dto.request.MarkPaidRequest;
import com.schoolmgmt.model.SchoolSubscription;
import com.schoolmgmt.model.SchoolSubscription.SubscriptionStatus;
import com.schoolmgmt.model.SubscriptionInvoice;
import com.schoolmgmt.service.BillingService;
import com.schoolmgmt.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/superadmin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SubscriptionAdminController {

    private final SubscriptionService subscriptionService;
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<SchoolSubscription> assignPlan(@Valid @RequestBody AssignSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.assignPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<SchoolSubscription>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<SchoolSubscription> getSubscription(@PathVariable String tenantId) {
        return ResponseEntity.ok(subscriptionService.getByTenantId(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SchoolSubscription> overrideStatus(@PathVariable UUID id,
                                                              @RequestBody Map<String, String> body) {
        SubscriptionStatus status = SubscriptionStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(subscriptionService.overrideStatus(id, status));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<SubscriptionInvoice>> getAllInvoices(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.getAllInvoices(pageable));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<SubscriptionInvoice> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getInvoiceById(id));
    }

    @PostMapping("/invoices/{id}/mark-paid")
    public ResponseEntity<SubscriptionInvoice> markAsPaid(@PathVariable UUID id,
                                                           @Valid @RequestBody MarkPaidRequest request) {
        SubscriptionInvoice invoice = subscriptionService.getInvoiceById(id);
        invoice.setManualPaymentRef(request.getPaymentReference());
        invoice.setManualPaymentNote(request.getNote());
        billingService.handlePaymentSuccess(invoice, null, SubscriptionInvoice.PaymentMode.MANUAL);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{id}/cancel")
    public ResponseEntity<SubscriptionInvoice> cancelInvoice(@PathVariable UUID id) {
        SubscriptionInvoice invoice = subscriptionService.getInvoiceById(id);
        invoice.setStatus(SubscriptionInvoice.InvoiceStatus.CANCELLED);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/generate/{tenantId}")
    public ResponseEntity<SubscriptionInvoice> generateInvoice(@PathVariable String tenantId) {
        SchoolSubscription subscription = subscriptionService.getByTenantId(tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generateInvoiceForSubscription(subscription));
    }

    @PostMapping("/invoices/{id}/adjust")
    public ResponseEntity<SubscriptionInvoice> adjustInvoice(@PathVariable UUID id,
                                                              @Valid @RequestBody AdjustInvoiceRequest request) {
        SubscriptionInvoice invoice = subscriptionService.getInvoiceById(id);
        invoice.setAdjustmentAmount(request.getAdjustmentAmount());
        invoice.setAdjustmentNote(request.getAdjustmentNote());
        invoice.setTotalAmount(
                invoice.getBaseAmount()
                        .add(invoice.getPerStudentAmount().multiply(
                                java.math.BigDecimal.valueOf(invoice.getBillableStudentCount())))
                        .add(request.getAdjustmentAmount()));
        return ResponseEntity.ok(invoice);
    }
}
