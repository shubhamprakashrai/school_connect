package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    @NotBlank(message = "Notification type is required")
    private String notificationType; // FEE_REMINDER, ATTENDANCE_ALERT, EXAM_NOTICE, ANNOUNCEMENT, etc.

    // Target: one of these should be provided
    private String recipientUserId;
    private String recipientRole;
    private String recipientClassId;

    private String dataPayload; // JSON string for navigation data
}
