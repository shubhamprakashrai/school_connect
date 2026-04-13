package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tenant-scoped payment destination shown to parents/students on the
 * Pay Fee screen. Multiple channels per tenant are supported (different
 * banks, multiple UPI handles, etc). Channel type is loose enum; one
 * channel may carry both bank fields and a UPI id.
 */
@Entity
@Table(name = "payment_channels", indexes = {
    @Index(name = "idx_pc_tenant", columnList = "tenant_id"),
    @Index(name = "idx_pc_active", columnList = "tenant_id, is_active")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentChannel extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    @Column(name = "label", nullable = false, length = 120)
    private String label;            // e.g. "HDFC current account"

    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc", length = 20)
    private String ifsc;

    @Column(name = "account_holder", length = 200)
    private String accountHolder;

    @Column(name = "upi_id", length = 120)
    private String upiId;

    @Column(name = "instructions", length = 500)
    private String instructions;     // e.g. "Use student roll # in remarks"

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum ChannelType { QR, BANK, UPI }
}
