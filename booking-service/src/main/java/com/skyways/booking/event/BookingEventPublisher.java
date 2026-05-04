package com.skyways.booking.event;

import com.skyways.booking.domain.Booking;

public interface BookingEventPublisher {

    void publish(String eventType, Booking booking);
}
