package com.skyways.booking.event;

import com.skyways.booking.domain.Booking;
import com.skyways.booking.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingEvent(
        String eventType,
        UUID bookingId,
        String userId,
        String flightId,
        BookingStatus status,
        BigDecimal totalAmount,
        String currency,
        Instant occurredAt
) {
    public static BookingEvent from(String eventType, Booking booking) {
        return new BookingEvent(
                eventType,
                booking.getId(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                Instant.now()
        );
    }
}
