package com.schoolmgmt.service;

import com.schoolmgmt.model.Conversation;
import com.schoolmgmt.model.Message;
import com.schoolmgmt.repository.ConversationRepository;
import com.schoolmgmt.repository.MessageRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Conversation createConversation(Conversation conversation) {
        String tenantId = TenantContext.getCurrentTenant();
        conversation.setTenantId(tenantId);
        log.info("Creating conversation: {} for tenant: {}", conversation.getTitle(), tenantId);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getConversations(UUID userId) {
        String tenantId = TenantContext.getCurrentTenant();
        return conversationRepository.findByParticipantIdsContainingAndTenantId(userId, tenantId);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId) {
        String tenantId = TenantContext.getCurrentTenant();
        return messageRepository.findByConversationIdAndTenantIdOrderBySentAtDesc(conversationId, tenantId);
    }

    @Transactional
    public Message sendMessage(Message message) {
        String tenantId = TenantContext.getCurrentTenant();
        message.setTenantId(tenantId);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        // Update conversation's last message
        conversationRepository.findById(message.getConversationId()).ifPresent(conv -> {
            conv.setLastMessage(message.getContent());
            conv.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conv);
        });

        log.info("Message sent in conversation: {}", message.getConversationId());
        return saved;
    }

    @Transactional
    public Message markAsRead(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found: " + messageId));
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        String tenantId = TenantContext.getCurrentTenant();
        return messageRepository.countByRecipientIdAndIsReadFalseAndTenantId(userId, tenantId);
    }
}
