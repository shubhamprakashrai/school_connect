package com.schoolmgmt.controller;

import com.schoolmgmt.model.RazorpayWebhookLog;
import com.schoolmgmt.model.RazorpayWebhookLog.ProcessingStatus;
import com.schoolmgmt.model.SubscriptionInvoice;
import com.schoolmgmt.repository.RazorpayWebhookLogRepository;
import com.schoolmgmt.repository.SubscriptionInvoiceRepository;
import com.schoolmgmt.service.BillingService;
import com.schoolmgmt.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/webhooks/razorpay")
@RequiredArgsConstructor
@Slf4j
public class RazorpayWebhookController {

    private final RazorpayService razorpayService;
    private final BillingService billingService;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final RazorpayWebhookLogRepository webhookLogRepository;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        if (signature == null || !razorpayService.verifyWebhookSignature(payload, signature)) {
            log.warn("Invalid webhook signature received");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventId = event.optString("id", "");
        String eventType = event.optString("event", "");

        if (webhookLogRepository.existsByEventId(eventId)) {
            log.info("Duplicate webhook event skipped: {}", eventId);
            return ResponseEntity.ok("Already processed");
        }

        JSONObject paymentEntity = event.optJSONObject("payload")
                .optJSONObject("payment")
                .optJSONObject("entity");

        String orderId = paymentEntity.optString("order_id", "");
        String paymentId = paymentEntity.optString("id", "");

        RazorpayWebhookLog webhookLog = RazorpayWebhookLog.builder()
                .eventId(eventId)
                .eventType(eventType)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .payload(payload)
                .processingStatus(ProcessingStatus.RECEIVED)
                .build();
        webhookLogRepository.save(webhookLog);

        try {
            if ("payment.captured".equals(eventType)) {
                handlePaymentCaptured(orderId, paymentId);
                webhookLog.setProcessingStatus(ProcessingStatus.PROCESSED);
            } else if ("payment.failed".equals(eventType)) {
                log.info("Payment failed for order: {}", orderId);
                webhookLog.setProcessingStatus(ProcessingStatus.PROCESSED);
            } else {
                log.info("Unhandled webhook event type: {}", eventType);
                webhookLog.setProcessingStatus(ProcessingStatus.SKIPPED);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook event {}: {}", eventId, e.getMessage());
            webhookLog.setProcessingStatus(ProcessingStatus.FAILED);
            webhookLog.setErrorMessage(e.getMessage());
        }

        webhookLogRepository.save(webhookLog);
        return ResponseEntity.ok("OK");
    }

    private void handlePaymentCaptured(String orderId, String paymentId) {
        Optional<SubscriptionInvoice> invoiceOpt = invoiceRepository.findByRazorpayOrderId(orderId);
        if (invoiceOpt.isEmpty()) {
            log.warn("No invoice found for Razorpay order: {}", orderId);
            return;
        }
        billingService.handlePaymentSuccess(invoiceOpt.get(), paymentId, SubscriptionInvoice.PaymentMode.RAZORPAY);
    }
}
