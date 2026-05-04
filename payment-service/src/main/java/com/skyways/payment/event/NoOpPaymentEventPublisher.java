package com.skyways.payment.event;

import com.skyways.payment.domain.Payment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publish(String eventType, Payment payment) {
    }
}
