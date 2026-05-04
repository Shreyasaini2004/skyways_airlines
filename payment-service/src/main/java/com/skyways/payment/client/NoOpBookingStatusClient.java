package com.skyways.payment.client;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.booking-service.client", havingValue = "noop", matchIfMissing = true)
public class NoOpBookingStatusClient implements BookingStatusClient {

    @Override
    public void markPaymentSucceeded(UUID bookingId) {
    }

    @Override
    public void markPaymentFailed(UUID bookingId) {
    }
}
