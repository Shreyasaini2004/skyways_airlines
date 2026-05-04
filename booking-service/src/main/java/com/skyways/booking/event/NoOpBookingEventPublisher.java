package com.skyways.booking.event;

import com.skyways.booking.domain.Booking;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpBookingEventPublisher implements BookingEventPublisher {

    @Override
    public void publish(String eventType, Booking booking) {
    }
}
