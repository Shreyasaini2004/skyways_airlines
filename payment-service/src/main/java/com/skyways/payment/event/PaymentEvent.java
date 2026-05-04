package com.skyways.payment.event;

import com.skyways.payment.domain.Payment;
import com.skyways.payment.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        String eventType,
        UUID paymentId,
        UUID bookingId,
        String userId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String failureReason,
        Instant occurredAt
) {
    public static PaymentEvent from(String eventType, Payment payment) {
        return new PaymentEvent(
                eventType,
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureReason(),
                Instant.now()
        );
    }
}
