package com.skyways.notification.controller;

import com.skyways.notification.dto.CreateNotificationRequest;
import com.skyways.notification.dto.NotificationResponse;
import com.skyways.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = NotificationResponse.from(notificationService.sendEmail(request));
        return ResponseEntity
                .created(URI.create("/api/notifications/" + response.id()))
                .body(response);
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getNotification(@PathVariable UUID notificationId) {
        return NotificationResponse.from(notificationService.getNotification(notificationId));
    }

    @GetMapping(params = "bookingId")
    public List<NotificationResponse> getNotificationsForBooking(@RequestParam UUID bookingId) {
        return notificationService.getNotificationsForBooking(bookingId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping(params = "userId")
    public List<NotificationResponse> getNotificationsForUser(@RequestParam @NotBlank String userId) {
        return notificationService.getNotificationsForUser(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
