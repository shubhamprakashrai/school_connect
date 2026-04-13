package com.schoolmgmt.dto.request;

import com.schoolmgmt.model.PaymentChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentChannelRequest {
    @NotNull
    private PaymentChannel.ChannelType channelType;

    @NotBlank
    @Size(max = 120)
    private String label;

    @Size(max = 500) private String qrImageUrl;
    @Size(max = 200) private String bankName;
    @Size(max = 50)  private String accountNumber;
    @Size(max = 20)  private String ifsc;
    @Size(max = 200) private String accountHolder;
    @Size(max = 120) private String upiId;
    @Size(max = 500) private String instructions;

    private Boolean isActive;
}
