package com.skyways.notification.service;

import com.skyways.notification.domain.Notification;
import com.skyways.notification.dto.CreateNotificationRequest;
import com.skyways.notification.exception.NotificationNotFoundException;
import com.skyways.notification.provider.EmailProvider;
import com.skyways.notification.provider.EmailProviderResult;
import com.skyways.notification.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailProvider emailProvider;

    public NotificationService(NotificationRepository notificationRepository, EmailProvider emailProvider) {
        this.notificationRepository = notificationRepository;
        this.emailProvider = emailProvider;
    }

    @Transactional
    public Notification sendEmail(CreateNotificationRequest request) {
        Notification notification = new Notification(
                request.bookingId(),
                request.userId(),
                request.recipient(),
                request.subject(),
                request.body()
        );

        EmailProviderResult result = emailProvider.send(request.recipient(), request.subject(), request.body());
        if (result.successful()) {
            notification.markSent(result.providerReference());
        } else {
            notification.markFailed(result.failureReason());
        }

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification getNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForBooking(UUID bookingId) {
        return notificationRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
