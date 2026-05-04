package com.skyways.payment.dto;

import com.skyways.payment.domain.Payment;
import com.skyways.payment.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        String userId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String paymentMethodLabel,
        String providerReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethodLabel(),
                payment.getProviderReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
