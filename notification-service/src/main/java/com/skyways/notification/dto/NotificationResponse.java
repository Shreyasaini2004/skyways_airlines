package com.skyways.notification.dto;

import com.skyways.notification.domain.Notification;
import com.skyways.notification.domain.NotificationChannel;
import com.skyways.notification.domain.NotificationStatus;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID bookingId,
        String userId,
        NotificationChannel channel,
        NotificationStatus status,
        String recipient,
        String subject,
        String body,
        String providerReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getBookingId(),
                notification.getUserId(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody(),
                notification.getProviderReference(),
                notification.getFailureReason(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
