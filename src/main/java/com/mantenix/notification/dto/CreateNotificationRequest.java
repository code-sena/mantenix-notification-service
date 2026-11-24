package com.mantenix.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Body of POST /notifications (CreateNotificationRequest in notification-service.yaml). */
public record CreateNotificationRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 20) String channel,
        @NotBlank @Size(max = 255) String recipient,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String message) {
}
