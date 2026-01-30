package com.schoolmgmt.controller;

import com.schoolmgmt.model.Conversation;
import com.schoolmgmt.model.Message;
import com.schoolmgmt.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Messaging", description = "APIs for managing conversations and messages")
public class MessagingController {

    private final MessagingService messagingService;

    @PostMapping("/conversations")
    @Operation(summary = "Create conversation")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Conversation> createConversation(@Valid @RequestBody Conversation conversation) {
        log.info("Creating conversation: {}", conversation.getTitle());
        Conversation created = messagingService.createConversation(conversation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get conversations for a user")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<Conversation>> getConversations(@RequestParam UUID userId) {
        return ResponseEntity.ok(messagingService.getConversations(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Get messages in a conversation")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID id) {
        return ResponseEntity.ok(messagingService.getMessages(id));
    }

    @PostMapping("/messages")
    @Operation(summary = "Send a message")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody Message message) {
        log.info("Sending message from: {}", message.getSenderId());
        Message sent = messagingService.sendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @PutMapping("/conversations/{id}/read")
    @Operation(summary = "Mark conversation messages as read")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        // Mark all unread messages in the conversation
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam UUID userId) {
        Long count = messagingService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
