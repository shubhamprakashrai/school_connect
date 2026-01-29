package com.schoolmgmt.repository;

import com.schoolmgmt.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findByTenantIdAndRecipientUserIdOrderByCreatedAtDesc(
            String tenantId, String recipientUserId, Pageable pageable);

    List<NotificationLog> findByTenantIdAndRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(
            String tenantId, String recipientUserId);

    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.tenantId = :tenantId " +
           "AND n.recipientUserId = :userId AND n.isRead = false")
    Long countUnreadByUser(@Param("tenantId") String tenantId, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.id = :id")
    void markAsRead(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.tenantId = :tenantId AND n.recipientUserId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("tenantId") String tenantId, @Param("userId") String userId);

    Page<NotificationLog> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
}
