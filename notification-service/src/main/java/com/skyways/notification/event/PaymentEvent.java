package com.skyways.notification.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        String eventType,
        UUID paymentId,
        UUID bookingId,
        String userId,
        String status,
        BigDecimal amount,
        String currency,
        String failureReason,
        Instant occurredAt
) {
}
