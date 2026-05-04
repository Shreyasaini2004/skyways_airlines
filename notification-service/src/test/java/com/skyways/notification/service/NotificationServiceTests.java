package com.skyways.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.skyways.notification.domain.Notification;
import com.skyways.notification.domain.NotificationStatus;
import com.skyways.notification.dto.CreateNotificationRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceTests {

    @Autowired
    private NotificationService notificationService;

    @Test
    void sendsMockEmail() {
        Notification notification = notificationService.sendEmail(new CreateNotificationRequest(
                UUID.randomUUID(),
                "user-1",
                "asha.rao@example.com",
                "Booking confirmed",
                "Your SkyWays booking is confirmed."
        ));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getProviderReference()).startsWith("mock_sendgrid_");
    }
}
