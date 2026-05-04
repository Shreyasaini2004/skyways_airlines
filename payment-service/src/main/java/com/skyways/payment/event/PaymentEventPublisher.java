package com.skyways.payment.event;

import com.skyways.payment.domain.Payment;

public interface PaymentEventPublisher {

    void publish(String eventType, Payment payment);
}
