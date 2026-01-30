package com.schoolmgmt.repository;

import com.schoolmgmt.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c WHERE :userId MEMBER OF c.participantIds AND c.tenantId = :tenantId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantIdsContainingAndTenantId(@Param("userId") UUID userId, @Param("tenantId") String tenantId);

    List<Conversation> findByTenantIdOrderByUpdatedAtDesc(String tenantId);
}
