package com.schoolmgmt.dto.response;

import com.schoolmgmt.model.PaymentChannel;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class PaymentChannelResponse {
    UUID id;
    String channelType;
    String label;
    String qrImageUrl;
    String bankName;
    String accountNumber;
    String ifsc;
    String accountHolder;
    String upiId;
    String instructions;
    boolean isActive;

    public static PaymentChannelResponse from(PaymentChannel c) {
        return PaymentChannelResponse.builder()
            .id(c.getId())
            .channelType(c.getChannelType().name())
            .label(c.getLabel())
            .qrImageUrl(c.getQrImageUrl())
            .bankName(c.getBankName())
            .accountNumber(c.getAccountNumber())
            .ifsc(c.getIfsc())
            .accountHolder(c.getAccountHolder())
            .upiId(c.getUpiId())
            .instructions(c.getInstructions())
            .isActive(Boolean.TRUE.equals(c.getIsActive()))
            .build();
    }
}
