package com.mantenix.notification.service;

import com.mantenix.notification.dto.CreateNotificationRequest;
import com.mantenix.notification.dto.NotificationResponse;
import com.mantenix.notification.dto.PageResponse;
import com.mantenix.notification.dto.UpdateNotificationRequest;
import com.mantenix.notification.exception.ResourceNotFoundException;
import com.mantenix.notification.model.Notification;
import com.mantenix.notification.model.NotificationStatus;
import com.mantenix.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Business rules of notifications. */
@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /** New notifications always start as PENDING; the delivery channel is still to be decided (DEC-004). */
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setType(request.type().trim());
        notification.setChannel(request.channel().trim());
        notification.setRecipient(request.recipient().trim());
        notification.setSubject(request.subject().trim());
        notification.setMessage(request.message());
        notification.setStatus(NotificationStatus.PENDING);
        return NotificationResponse.from(repository.save(notification));
    }

    /** Active notifications, newest first. Every filter is optional. page starts at 1. */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findAll(int page, int limit, NotificationStatus status,
                                                      Boolean read, UUID userId) {
        Specification<Notification> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (read != null) {
            spec = spec.and((root, query, cb) ->
                    read ? cb.isNotNull(root.get("readAt")) : cb.isNull(root.get("readAt")));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(repository.findAll(spec, pageRequest), NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        return NotificationResponse.from(getActive(id));
    }

    /** read = true keeps the first reading date if it was already read; read = false clears it. */
    @Transactional
    public NotificationResponse update(UUID id, UpdateNotificationRequest request) {
        Notification notification = getActive(id);
        if (request.read()) {
            if (notification.getReadAt() == null) {
                notification.setReadAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
        } else {
            notification.setReadAt(null);
        }
        // saveAndFlush so the response already carries the new updatedAt
        return NotificationResponse.from(repository.saveAndFlush(notification));
    }

    /** Soft delete: the row is kept with deleted_at filled in. */
    @Transactional
    public void delete(UUID id) {
        Notification notification = getActive(id);
        notification.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private Notification getActive(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " not found"));
    }
}
