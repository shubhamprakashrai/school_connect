package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.PaymentSubmission;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class PaymentSubmissionResponse {
    UUID id;
    UUID studentId;
    UUID feeId;
    UUID channelId;
    BigDecimal amount;
    String screenshotUrl;
    String remarks;
    String status;
    LocalDateTime submittedAt;
    String reviewedBy;
    LocalDateTime reviewedAt;
    String rejectionReason;

    public static PaymentSubmissionResponse from(PaymentSubmission s) {
        return PaymentSubmissionResponse.builder()
            .id(s.getId())
            .studentId(s.getStudentId())
            .feeId(s.getFeeId())
            .channelId(s.getChannelId())
            .amount(s.getAmount())
            .screenshotUrl(s.getScreenshotUrl())
            .remarks(s.getRemarks())
            .status(s.getStatus().name())
            .submittedAt(s.getSubmittedAt())
            .reviewedBy(s.getReviewedBy())
            .reviewedAt(s.getReviewedAt())
            .rejectionReason(s.getRejectionReason())
            .build();
    }
}
