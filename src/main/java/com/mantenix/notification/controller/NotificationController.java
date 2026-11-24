package com.mantenix.notification.controller;

import com.mantenix.notification.dto.CreateNotificationRequest;
import com.mantenix.notification.dto.NotificationResponse;
import com.mantenix.notification.dto.PageResponse;
import com.mantenix.notification.dto.UpdateNotificationRequest;
import com.mantenix.notification.model.NotificationStatus;
import com.mantenix.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/** REST endpoints for notifications (07-api/contracts/openapi/notification-service.yaml). */
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Operations on user notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a notification", description = "Created with status PENDING")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/notifications/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List notifications", description = "Newest first. Optional filters: status, read, userId")
    public PageResponse<NotificationResponse> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) UUID userId) {
        return service.findAll(page, limit, status, read, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id")
    public NotificationResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mark a notification as read or unread")
    public NotificationResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateNotificationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a notification", description = "Soft delete: the record is kept with deleted_at filled in")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
