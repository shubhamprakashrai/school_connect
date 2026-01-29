package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.FcmTokenRequest;
import com.schoolmgmt.dto.request.SendNotificationRequest;
import com.schoolmgmt.dto.ApiResponse;
import com.schoolmgmt.model.FcmToken;
import com.schoolmgmt.model.NotificationLog;
import com.schoolmgmt.model.NotificationTemplate;
import com.schoolmgmt.model.User;
import com.schoolmgmt.service.FcmTokenService;
import com.schoolmgmt.service.NotificationService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for notification and FCM token management.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification and FCM token management APIs")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final FcmTokenService fcmTokenService;
    private final NotificationService notificationService;

    /**
     * Register FCM token for push notifications
     */
    @PostMapping("/token")
    @Operation(summary = "Register FCM token", description = "Register or update FCM token for the authenticated user")
    public ResponseEntity<ApiResponse> registerToken(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FcmTokenRequest request) {

        log.info("Registering FCM token for user: {}", user.getUserId());

        FcmToken token = fcmTokenService.registerToken(user, request);

        Map<String, Object> response = Map.of(
            "tokenId", token.getId().toString(),
            "registered", true,
            "message", "FCM token registered successfully"
        );

        return ResponseEntity.ok(ApiResponse.success("FCM token registered successfully", response));
    }

    /**
     * Remove FCM token
     */
    @DeleteMapping("/token")
    @Operation(summary = "Remove FCM token", description = "Remove FCM token for the authenticated user")
    public ResponseEntity<ApiResponse> removeToken(
            @AuthenticationPrincipal User user,
            @RequestParam String token) {

        log.info("Removing FCM token for user: {}", user.getUserId());

        fcmTokenService.removeToken(token);

        Map<String, Object> response = Map.of(
            "removed", true,
            "message", "FCM token removed successfully"
        );

        return ResponseEntity.ok(ApiResponse.success("FCM token removed successfully", response));
    }

    /**
     * Deactivate FCM token (soft delete)
     */
    @PostMapping("/token/deactivate")
    @Operation(summary = "Deactivate FCM token", description = "Deactivate FCM token without deleting")
    public ResponseEntity<ApiResponse> deactivateToken(
            @AuthenticationPrincipal User user,
            @RequestParam String token) {

        log.info("Deactivating FCM token for user: {}", user.getUserId());

        fcmTokenService.deactivateToken(token);

        Map<String, Object> response = Map.of(
            "deactivated", true,
            "message", "FCM token deactivated successfully"
        );

        return ResponseEntity.ok(ApiResponse.success("FCM token deactivated successfully", response));
    }

    /**
     * Get all active tokens for current user
     */
    @GetMapping("/tokens")
    @Operation(summary = "Get user's FCM tokens", description = "Get all active FCM tokens for the authenticated user")
    public ResponseEntity<ApiResponse> getUserTokens(
            @AuthenticationPrincipal User user) {

        List<FcmToken> tokens = fcmTokenService.getActiveTokensForUser(user.getUserId());

        List<Map<String, Object>> tokenList = tokens.stream()
            .map(t -> Map.<String, Object>of(
                "id", t.getId().toString(),
                "deviceType", t.getDeviceType() != null ? t.getDeviceType().name() : "UNKNOWN",
                "deviceName", t.getDeviceName() != null ? t.getDeviceName() : "",
                "lastUsedAt", t.getLastUsedAt() != null ? t.getLastUsedAt().toString() : "",
                "createdAt", t.getCreatedAt().toString()
            ))
            .toList();

        return ResponseEntity.ok(ApiResponse.success("Tokens retrieved successfully", tokenList));
    }

    /**
     * Remove all tokens for current user (logout from all devices)
     */
    @DeleteMapping("/tokens/all")
    @Operation(summary = "Remove all FCM tokens", description = "Remove all FCM tokens for the authenticated user (logout from all devices)")
    public ResponseEntity<ApiResponse> removeAllTokens(
            @AuthenticationPrincipal User user) {

        log.info("Removing all FCM tokens for user: {}", user.getUserId());

        fcmTokenService.removeAllTokensForUser(user.getUserId());

        Map<String, Object> response = Map.of(
            "removed", true,
            "message", "All FCM tokens removed successfully"
        );

        return ResponseEntity.ok(ApiResponse.success("All FCM tokens removed successfully", response));
    }

    // ===== Send Notification Endpoints =====

    @PostMapping("/send")
    @Operation(summary = "Send notification", description = "Send notification to a user, role, or class")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<NotificationLog> sendNotification(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SendNotificationRequest request) {

        log.info("Sending notification: {} by user: {}", request.getTitle(), user.getUserId());

        NotificationLog.NotificationType type;
        try {
            type = NotificationLog.NotificationType.valueOf(request.getNotificationType());
        } catch (IllegalArgumentException e) {
            type = NotificationLog.NotificationType.CUSTOM;
        }

        NotificationLog notification;
        if (request.getRecipientUserId() != null) {
            notification = notificationService.sendToUser(
                    request.getRecipientUserId(), request.getTitle(), request.getBody(),
                    type, request.getDataPayload(), user.getUserId(),
                    user.getFirstName() + " " + user.getLastName());
        } else if (request.getRecipientClassId() != null) {
            notification = notificationService.sendToClass(
                    UUID.fromString(request.getRecipientClassId()),
                    request.getTitle(), request.getBody(), type,
                    request.getDataPayload(), user.getUserId(),
                    user.getFirstName() + " " + user.getLastName());
        } else if (request.getRecipientRole() != null) {
            List<NotificationLog> notifications = notificationService.sendToRole(
                    request.getRecipientRole(), request.getTitle(), request.getBody(),
                    type, request.getDataPayload(), user.getUserId(),
                    user.getFirstName() + " " + user.getLastName());
            notification = notifications.isEmpty() ? null : notifications.get(0);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    // ===== User Notification History =====

    @GetMapping("/user")
    @Operation(summary = "Get my notifications", description = "Get notification history for the authenticated user")
    public ResponseEntity<Page<NotificationLog>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(user.getUserId(), pageable));
    }

    @GetMapping("/user/unread")
    @Operation(summary = "Get unread notifications", description = "Get unread notifications for the authenticated user")
    public ResponseEntity<List<NotificationLog>> getUnreadNotifications(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(user.getUserId()));
    }

    @GetMapping("/user/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @AuthenticationPrincipal User user) {
        Long count = notificationService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getUserId());
        return ResponseEntity.ok().build();
    }

    // ===== Admin: All Notifications =====

    @GetMapping("/all")
    @Operation(summary = "Get all notifications", description = "Get all notification logs (admin)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<NotificationLog>> getAllNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    // ===== Notification Templates =====

    @PostMapping("/templates")
    @Operation(summary = "Create template", description = "Create a notification template")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<NotificationTemplate> createTemplate(
            @Valid @RequestBody NotificationTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createTemplate(template));
    }

    @GetMapping("/templates")
    @Operation(summary = "Get templates", description = "Get all notification templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<NotificationTemplate>> getTemplates() {
        return ResponseEntity.ok(notificationService.getTemplates());
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "Update template", description = "Update a notification template")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable UUID id, @Valid @RequestBody NotificationTemplate template) {
        return ResponseEntity.ok(notificationService.updateTemplate(id, template));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "Delete template", description = "Delete a notification template")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        notificationService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
