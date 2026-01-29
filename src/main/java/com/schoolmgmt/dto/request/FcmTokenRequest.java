package com.schoolmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering FCM token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    private String deviceType;  // ANDROID, IOS, WEB

    private String deviceId;

    private String deviceName;

    private String appVersion;
}
