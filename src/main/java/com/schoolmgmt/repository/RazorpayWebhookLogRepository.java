package com.schoolmgmt.repository;

import com.schoolmgmt.model.RazorpayWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RazorpayWebhookLogRepository extends JpaRepository<RazorpayWebhookLog, UUID> {
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
    boolean existsByEventId(String eventId);
}
