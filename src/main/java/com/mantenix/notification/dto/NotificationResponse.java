package com.mantenix.notification.dto;

import com.mantenix.notification.model.Notification;
import com.mantenix.notification.model.NotificationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Notification returned by the API. Internal fields (errorMessage, sourceEventId, deletedAt)
 * are not exposed.
 */
public record NotificationResponse(
        UUID id,
        UUID userId,
        String type,
        String channel,
        String recipient,
        String subject,
        String message,
        NotificationStatus status,
        OffsetDateTime sentAt,
        OffsetDateTime readAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getChannel(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}
