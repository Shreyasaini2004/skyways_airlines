package com.skyways.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyways.notification.dto.CreateNotificationRequest;
import com.skyways.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.kafka.enabled", havingValue = "true")
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public PaymentEventListener(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${skyways.kafka.topics.payment-events}", groupId = "notification-service")
    public void onPaymentEvent(String payload) {
        try {
            PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
            if ("PAYMENT_SUCCEEDED".equals(event.eventType())) {
                notificationService.sendEmail(new CreateNotificationRequest(
                        event.bookingId(),
                        event.userId(),
                        recipientFor(event.userId()),
                        "SkyWays booking confirmed",
                        "Your payment was successful and your SkyWays booking is confirmed."
                ));
            } else if ("PAYMENT_FAILED".equals(event.eventType())) {
                notificationService.sendEmail(new CreateNotificationRequest(
                        event.bookingId(),
                        event.userId(),
                        recipientFor(event.userId()),
                        "SkyWays payment failed",
                        "Your payment could not be completed. Reason: " + event.failureReason()
                ));
            }
        } catch (JsonProcessingException ex) {
            log.warn("Unable to process payment event payload: {}", payload, ex);
        }
    }

    private String recipientFor(String userId) {
        if (userId.contains("@")) {
            return userId;
        }
        return userId + "@example.com";
    }
}
