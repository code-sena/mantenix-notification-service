package com.mantenix.notification.service;

import com.mantenix.notification.dto.CreateNotificationRequest;
import com.mantenix.notification.dto.NotificationResponse;
import com.mantenix.notification.dto.UpdateNotificationRequest;
import com.mantenix.notification.exception.ResourceNotFoundException;
import com.mantenix.notification.model.Notification;
import com.mantenix.notification.model.NotificationStatus;
import com.mantenix.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the business rules of NotificationService (no database needed). */
class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationService(repository);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAndFlush(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createStartsAsPendingAndUnread() {
        UUID userId = UUID.randomUUID();

        NotificationResponse response = service.create(new CreateNotificationRequest(
                userId, "WELCOME", "EMAIL", "user@example.com", "Welcome to Mantenix", "Your account is ready."));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(response.readAt()).isNull();
        assertThat(response.sentAt()).isNull();
    }

    @Test
    void markAsReadSetsTheReadDate() {
        Notification notification = existing();

        NotificationResponse response = service.update(notification.getId(), new UpdateNotificationRequest(true));

        assertThat(response.readAt()).isNotNull();
    }

    @Test
    void markAsReadAgainKeepsTheFirstReadDate() {
        Notification notification = existing();
        OffsetDateTime firstRead = OffsetDateTime.of(2026, 11, 20, 10, 0, 0, 0, ZoneOffset.UTC);
        notification.setReadAt(firstRead);

        NotificationResponse response = service.update(notification.getId(), new UpdateNotificationRequest(true));

        assertThat(response.readAt()).isEqualTo(firstRead);
    }

    @Test
    void markAsUnreadClearsTheReadDate() {
        Notification notification = existing();
        notification.setReadAt(OffsetDateTime.now(ZoneOffset.UTC));

        NotificationResponse response = service.update(notification.getId(), new UpdateNotificationRequest(false));

        assertThat(response.readAt()).isNull();
    }

    @Test
    void deleteIsASoftDelete() {
        Notification notification = existing();

        service.delete(notification.getId());

        assertThat(notification.getDeletedAt()).isNotNull();
        verify(repository, never()).delete(any(Notification.class));
    }

    @Test
    void findByIdFailsWhenTheNotificationDoesNotExistOrWasDeleted() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Notification existing() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(UUID.randomUUID());
        when(repository.findByIdAndDeletedAtIsNull(notification.getId())).thenReturn(Optional.of(notification));
        return notification;
    }
}
