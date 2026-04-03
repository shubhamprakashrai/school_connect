package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.RechargeRequest;
import com.schoolmgmt.model.SchoolSubscription;
import com.schoolmgmt.model.SubscriptionInvoice;
import com.schoolmgmt.service.BillingService;
import com.schoolmgmt.service.RazorpayService;
import com.schoolmgmt.service.SubscriptionService;
import com.schoolmgmt.util.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class SubscriptionSelfController {

    private final SubscriptionService subscriptionService;
    private final BillingService billingService;
    private final RazorpayService razorpayService;

    @GetMapping("/current")
    public ResponseEntity<SchoolSubscription> getCurrentSubscription() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(subscriptionService.getByTenantId(tenantId));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<SubscriptionInvoice>> getMyInvoices() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(subscriptionService.getInvoicesByTenant(tenantId));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<SubscriptionInvoice> getInvoiceDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getInvoiceById(id));
    }

    @PostMapping("/pay/{invoiceId}")
    public ResponseEntity<Map<String, Object>> initiatePayment(@PathVariable UUID invoiceId) {
        SubscriptionInvoice invoice = subscriptionService.getInvoiceById(invoiceId);
        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", invoice.getRazorpayOrderId());
        response.put("razorpayKey", razorpayService.getApiKey());
        response.put("amount", invoice.getTotalAmount());
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> recharge(@Valid @RequestBody RechargeRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        SchoolSubscription subscription = subscriptionService.getByTenantId(tenantId);
        SubscriptionInvoice invoice = billingService.createRechargeInvoice(subscription, request.getMonths());

        Map<String, Object> response = new HashMap<>();
        response.put("invoice", invoice);
        response.put("razorpayOrderId", invoice.getRazorpayOrderId());
        response.put("razorpayKey", razorpayService.getApiKey());
        response.put("amount", invoice.getTotalAmount());
        return ResponseEntity.ok(response);
    }
}
