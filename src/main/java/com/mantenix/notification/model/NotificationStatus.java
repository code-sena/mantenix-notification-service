package com.mantenix.notification.model;

/** Delivery status of a notification (CHECK ck_notifications_status in V1). */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
