package com.schoolmgmt.service;

import com.google.firebase.messaging.*;
import com.schoolmgmt.model.FcmToken;
import com.schoolmgmt.model.NotificationLog;
import com.schoolmgmt.model.NotificationTemplate;
import com.schoolmgmt.repository.FcmTokenRepository;
import com.schoolmgmt.repository.NotificationLogRepository;
import com.schoolmgmt.repository.NotificationTemplateRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    @Nullable
    private final FirebaseMessaging firebaseMessaging;

    // ===== Send Notifications =====

    @Transactional
    public NotificationLog sendToUser(String recipientUserId, String title, String body,
                                       NotificationLog.NotificationType type,
                                       String dataPayload, String senderUserId, String senderName) {
        String tenantId = TenantContext.getCurrentTenant();

        // Create notification log
        NotificationLog notification = NotificationLog.builder()
                .tenantId(tenantId)
                .title(title)
                .body(body)
                .notificationType(type)
                .recipientUserId(recipientUserId)
                .senderUserId(senderUserId)
                .senderName(senderName)
                .dataPayload(dataPayload)
                .status(NotificationLog.NotificationStatus.PENDING)
                .build();

        // Get user's FCM tokens
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(recipientUserId);

        if (tokens.isEmpty()) {
            log.warn("No active FCM tokens found for user: {}", recipientUserId);
            notification.setStatus(NotificationLog.NotificationStatus.FAILED);
            notification.setErrorMessage("No active FCM tokens for user");
        } else {
            // Send push notification via FCM
            boolean sent = sendPushNotification(tokens, title, body, dataPayload);
            notification.setStatus(sent
                    ? NotificationLog.NotificationStatus.SENT
                    : NotificationLog.NotificationStatus.FAILED);
            if (!sent) {
                notification.setErrorMessage("FCM send failed");
            }
        }

        return notificationLogRepository.save(notification);
    }

    @Transactional
    public List<NotificationLog> sendToRole(String role, String title, String body,
                                             NotificationLog.NotificationType type,
                                             String dataPayload, String senderUserId, String senderName) {
        String tenantId = TenantContext.getCurrentTenant();
        List<NotificationLog> notifications = new ArrayList<>();

        // Get all tokens for the tenant
        List<FcmToken> allTokens = fcmTokenRepository.findByTenantIdAndIsActiveTrue(
                tenantId);

        // Log notification for the role (broadcast)
        NotificationLog notification = NotificationLog.builder()
                .tenantId(tenantId)
                .title(title)
                .body(body)
                .notificationType(type)
                .recipientRole(role)
                .senderUserId(senderUserId)
                .senderName(senderName)
                .dataPayload(dataPayload)
                .status(NotificationLog.NotificationStatus.PENDING)
                .build();

        if (!allTokens.isEmpty()) {
            boolean sent = sendPushNotification(allTokens, title, body, dataPayload);
            notification.setStatus(sent
                    ? NotificationLog.NotificationStatus.SENT
                    : NotificationLog.NotificationStatus.FAILED);
        } else {
            notification.setStatus(NotificationLog.NotificationStatus.FAILED);
            notification.setErrorMessage("No active tokens found");
        }

        notifications.add(notificationLogRepository.save(notification));
        return notifications;
    }

    @Transactional
    public NotificationLog sendToClass(UUID classId, String title, String body,
                                        NotificationLog.NotificationType type,
                                        String dataPayload, String senderUserId, String senderName) {
        String tenantId = TenantContext.getCurrentTenant();

        NotificationLog notification = NotificationLog.builder()
                .tenantId(tenantId)
                .title(title)
                .body(body)
                .notificationType(type)
                .recipientClassId(classId)
                .senderUserId(senderUserId)
                .senderName(senderName)
                .dataPayload(dataPayload)
                .status(NotificationLog.NotificationStatus.SENT)
                .build();

        log.info("Sending notification to class {}: {}", classId, title);
        return notificationLogRepository.save(notification);
    }

    // ===== User Notifications =====

    @Transactional(readOnly = true)
    public Page<NotificationLog> getUserNotifications(String userId, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationLogRepository.findByTenantIdAndRecipientUserIdOrderByCreatedAtDesc(
                tenantId, userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getUnreadNotifications(String userId) {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationLogRepository.findByTenantIdAndRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(
                tenantId, userId);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationLogRepository.countUnreadByUser(tenantId, userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationLogRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        String tenantId = TenantContext.getCurrentTenant();
        notificationLogRepository.markAllAsRead(tenantId, userId);
    }

    // ===== Notification Templates =====

    @Transactional
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        String tenantId = TenantContext.getCurrentTenant();
        template.setTenantId(tenantId);
        return notificationTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationTemplateRepository.findByTenantIdOrderByName(tenantId);
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplate> getActiveTemplates() {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationTemplateRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public NotificationTemplate updateTemplate(UUID id, NotificationTemplate updated) {
        NotificationTemplate existing = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getType() != null) existing.setType(updated.getType());
        if (updated.getTitleTemplate() != null) existing.setTitleTemplate(updated.getTitleTemplate());
        if (updated.getBodyTemplate() != null) existing.setBodyTemplate(updated.getBodyTemplate());
        if (updated.getChannel() != null) existing.setChannel(updated.getChannel());
        if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());

        return notificationTemplateRepository.save(existing);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        notificationTemplateRepository.deleteById(id);
    }

    // ===== All Notifications (Admin) =====

    @Transactional(readOnly = true)
    public Page<NotificationLog> getAllNotifications(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return notificationLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    // ===== Internal: FCM Push =====

    private boolean sendPushNotification(List<FcmToken> tokens, String title, String body,
                                          String dataPayload) {
        if (firebaseMessaging == null) {
            log.warn("Firebase not configured. Notification logged but not delivered: {}", title);
            return true; // Still mark as sent so notifications are saved
        }

        String tenantId = TenantContext.getCurrentTenant();
        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .toList();

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Build data map with tenant context
        Map<String, String> data = new HashMap<>();
        data.put("tenantId", tenantId != null ? tenantId : "");
        if (dataPayload != null && !dataPayload.isBlank()) {
            data.put("payload", dataPayload);
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .addAllTokens(tokenStrings)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("FCM multicast sent: {}/{} successful for tenant: {}",
                    response.getSuccessCount(), tokenStrings.size(), tenantId);

            // Handle invalid tokens - deactivate them
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String failedToken = tokenStrings.get(i);
                        FirebaseMessagingException ex = responses.get(i).getException();
                        if (ex != null && isInvalidTokenError(ex)) {
                            log.info("Deactivating invalid FCM token: {}...",
                                    failedToken.substring(0, Math.min(20, failedToken.length())));
                            fcmTokenRepository.findByToken(failedToken)
                                    .ifPresent(t -> {
                                        t.setIsActive(false);
                                        fcmTokenRepository.save(t);
                                    });
                        }
                    }
                }
            }

            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for tenant {}: {}", tenantId, e.getMessage());
            return false;
        }
    }

    private boolean isInvalidTokenError(FirebaseMessagingException ex) {
        return ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || ex.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
