package com.skyways.notification.repository;

import com.skyways.notification.domain.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}
