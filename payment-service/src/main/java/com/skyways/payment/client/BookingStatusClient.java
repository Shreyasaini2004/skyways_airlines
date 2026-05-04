package com.skyways.payment.client;

import java.util.UUID;

public interface BookingStatusClient {

    void markPaymentSucceeded(UUID bookingId);

    void markPaymentFailed(UUID bookingId);
}
