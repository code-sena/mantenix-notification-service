package com.mantenix.notification.dto;

import jakarta.validation.constraints.NotNull;

/** Body of PATCH /notifications/{id}: true marks the notification as read, false as unread. */
public record UpdateNotificationRequest(@NotNull Boolean read) {
}
