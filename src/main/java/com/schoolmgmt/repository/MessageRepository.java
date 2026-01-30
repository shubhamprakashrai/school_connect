package com.schoolmgmt.repository;

import com.schoolmgmt.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdAndTenantIdOrderBySentAtDesc(UUID conversationId, String tenantId, Pageable pageable);

    List<Message> findByConversationIdAndTenantIdOrderBySentAtDesc(UUID conversationId, String tenantId);

    List<Message> findByConversationIdAndIsReadFalseAndRecipientIdAndTenantId(UUID conversationId, UUID recipientId, String tenantId);

    Long countByRecipientIdAndIsReadFalseAndTenantId(UUID recipientId, String tenantId);
}
