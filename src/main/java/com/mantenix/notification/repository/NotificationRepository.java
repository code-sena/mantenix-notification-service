package com.mantenix.notification.repository;

import com.mantenix.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for the notifications table.
 * JpaSpecificationExecutor allows combining the optional list filters (status, read, userId).
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID>,
        JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByIdAndDeletedAtIsNull(UUID id);
}
