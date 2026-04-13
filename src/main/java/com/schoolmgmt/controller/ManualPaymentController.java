package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.PaymentChannelRequest;
import com.schoolmgmt.dto.request.PaymentSubmissionRequest;
import com.schoolmgmt.dto.response.PaymentChannelResponse;
import com.schoolmgmt.dto.response.PaymentSubmissionResponse;
import com.schoolmgmt.model.PaymentSubmission;
import com.schoolmgmt.security.Permission;
import com.schoolmgmt.security.RequirePermission;
import com.schoolmgmt.service.PaymentChannelService;
import com.schoolmgmt.service.PaymentSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manual fee payment workflow — payment channels (admin-managed) +
 * submissions (parent/student-uploaded) + approvals (anyone with
 * {@code FEES_APPROVE}). All endpoints tenant-scoped; auth-checked
 * by Spring Security; permission-guarded via {@link RequirePermission}.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ManualPaymentController {

    private final PaymentChannelService channelService;
    private final PaymentSubmissionService submissionService;

    // ── Channels ──────────────────────────────────────────────────────
    @GetMapping("/payment-channels")
    public ResponseEntity<List<PaymentChannelResponse>> listChannels(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        var list = (activeOnly ? channelService.listActive()
                                : channelService.listAll())
            .stream().map(PaymentChannelResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/payment-channels")
    @RequirePermission(Permission.PAYMENT_CONFIG)
    public ResponseEntity<PaymentChannelResponse> createChannel(
            @Valid @RequestBody PaymentChannelRequest req) {
        return ResponseEntity.ok(
            PaymentChannelResponse.from(channelService.create(req)));
    }

    @PutMapping("/payment-channels/{id}")
    @RequirePermission(Permission.PAYMENT_CONFIG)
    public ResponseEntity<PaymentChannelResponse> updateChannel(
            @PathVariable UUID id, @Valid @RequestBody PaymentChannelRequest req) {
        return ResponseEntity.ok(
            PaymentChannelResponse.from(channelService.update(id, req)));
    }

    @DeleteMapping("/payment-channels/{id}")
    @RequirePermission(Permission.PAYMENT_CONFIG)
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Submissions ────────────────────────────────────────────────────
    @PostMapping("/payment-submissions")
    @RequirePermission(Permission.PAYMENT_SUBMIT)
    public ResponseEntity<PaymentSubmissionResponse> submit(
            @Valid @RequestBody PaymentSubmissionRequest req) {
        return ResponseEntity.ok(
            PaymentSubmissionResponse.from(submissionService.submit(req)));
    }

    @GetMapping("/payment-submissions")
    @RequirePermission(Permission.FEES_APPROVE)
    public ResponseEntity<Page<PaymentSubmissionResponse>> list(
            @RequestParam(required = false) PaymentSubmission.SubmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = submissionService.list(status, PageRequest.of(page, size))
            .map(PaymentSubmissionResponse::from);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payment-submissions/student/{studentId}")
    public ResponseEntity<Page<PaymentSubmissionResponse>> listForStudent(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = submissionService
            .listForStudent(studentId, PageRequest.of(page, size))
            .map(PaymentSubmissionResponse::from);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payment-submissions/{id}")
    public ResponseEntity<PaymentSubmissionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
            PaymentSubmissionResponse.from(submissionService.get(id)));
    }

    @PostMapping("/payment-submissions/{id}/approve")
    @RequirePermission(Permission.FEES_APPROVE)
    public ResponseEntity<PaymentSubmissionResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(
            PaymentSubmissionResponse.from(submissionService.approve(id)));
    }

    @PostMapping("/payment-submissions/{id}/reject")
    @RequirePermission(Permission.FEES_APPROVE)
    public ResponseEntity<PaymentSubmissionResponse> reject(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            PaymentSubmissionResponse.from(
                submissionService.reject(id, body.getOrDefault("reason", ""))));
    }
}
